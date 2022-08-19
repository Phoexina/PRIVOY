package ch.ethz.infk.pps.zeph.driver;

import ch.ethz.infk.pps.shared.avro.DeltaUniverseState;
import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.client.facade.IPrivacyControllerFacade;
import ch.ethz.infk.pps.zeph.client.util.ClientConfig;
import ch.ethz.infk.pps.zeph.client.util.PrivacyState;
import ch.ethz.infk.pps.zeph.client.util.SQLUtil;
import ch.ethz.infk.pps.zeph.client.util.UniverseState;
import ch.ethz.infk.pps.zeph.crypto.Heac;
import ch.ethz.infk.pps.zeph.crypto.SecureAggregationNative;
import ch.ethz.infk.pps.zeph.crypto.util.ErdosRenyiUtil;
import ch.ethz.infk.pps.zeph.shared.DigestOp;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.ImmutableUnorderedPair;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowStatus;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowedUniverseId;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import javax.crypto.SecretKey;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.checkerframework.checker.units.qual.Current;

public class privacycontroller implements Runnable {
    private static final Logger LOG = LogManager.getLogger();
    private final IPrivacyControllerFacade facade;
    private final Map<Long, SecureAggregationNative> secureAggregation;
    private Map<Long, CompletableFuture<UniverseState>> universeChains;
    private Map<Long, CompletableFuture<PrivacyState>> producerChains;
    private Multimap<Long, Long> assignedProducers;
    private Map<Long, Long> stagedUniverseBoundary;
    private Map<Long, Long> mergedUniverseBoundary;
    private boolean isShutdownRequested;

    public privacycontroller(IPrivacyControllerFacade facade) {
        this.secureAggregation = new HashMap();
        this.universeChains = new HashMap();
        this.producerChains = new HashMap();
        this.assignedProducers = HashMultimap.create();
        this.stagedUniverseBoundary = new HashMap();
        this.mergedUniverseBoundary = new HashMap();
        this.isShutdownRequested = false;
        this.facade = facade;
    }

    public privacycontroller(List<ClientConfig> clientConfigs, Map<Long, Universe> universes, IPrivacyControllerFacade facade) {
        this(facade);
        universes.forEach((universeId, universe) -> {
            this.initUniverse(universeId, universe);
        });
        clientConfigs.forEach((config) -> {
            this.addAssignment(config.getUniverseId(), config);
        });
    }

    public void addSharedKeys(long universeId, Map<ImmutableUnorderedPair<Long>, SecretKey> sharedKeys) {
        ((SecureAggregationNative)this.secureAggregation.get(universeId)).addSharedKeys(sharedKeys);
    }

    public Multimap<Long, Long> getAssignedProducers() {
        return this.assignedProducers;
    }

    public void initUniverse(long universeId, Universe universe) {
        Window firstWindow = universe.getFirstWindow();
        long windowSize = firstWindow.getEnd() - firstWindow.getStart();
        Integer k = ErdosRenyiUtil.getK(universe.getMinimalSize(), universe.getAlpha(), universe.getDelta());
        UniverseState universeState;
        if (k != null) {
            int W = ErdosRenyiUtil.getNumberOfGraphs(k);
            long epochSize = (long)W * windowSize;
            UniverseState.UniverseConfig config = new UniverseState.UniverseConfig(windowSize, k, epochSize);
            universeState = new UniverseState(config);
            universeState.setMembers(Collections.emptySet());
            universeState.setEpochs(new ConcurrentHashMap());
            LOG.info("Using ER Secure Aggregation for universe {} with k={}", universeId, k);
        } else {
            UniverseState.UniverseConfig config = new UniverseState.UniverseConfig(windowSize);
            universeState = new UniverseState(config);
            universeState.setMembers(Collections.emptySet());
            LOG.info("Using Standard Secure Aggregation for universe {}", universeId);
        }

        this.universeChains.put(universeId, CompletableFuture.completedFuture(universeState));
        this.stagedUniverseBoundary.put(universeId, firstWindow.getStart());
        this.mergedUniverseBoundary.put(universeId, firstWindow.getStart());
        this.secureAggregation.put(universeId, new SecureAggregationNative());
    }

    public void addAssignment(long universeId, ClientConfig clientConfig) {
        long producerId = clientConfig.getProducerId();
        Heac heac = new Heac(clientConfig.getHeacKey());
        PrivacyState.PrivacyConfig privacyConfig = new PrivacyState.PrivacyConfig(heac);
        PrivacyState privacyState = new PrivacyState(privacyConfig);
        this.producerChains.put(producerId, CompletableFuture.completedFuture(privacyState));
        this.assignedProducers.put(universeId, producerId);
        if (clientConfig.getSharedKeys() != null) {
            this.addSharedKeys(universeId, clientConfig.getSharedKeys());
        }

    }

    public void run() {
        this.facade.init(this.universeChains.keySet());

        try {
            while(true) {
                ConsumerRecords<WindowedUniverseId, DeltaUniverseState> records = this.facade.pollInfos();
                if (this.isShutdownRequested) {
                    break;
                }

                records.forEach((record) -> {
                    WindowedUniverseId windowedUniverseId = (WindowedUniverseId)record.key();
                    DeltaUniverseState windowState = (DeltaUniverseState)record.value();
                    int statusCode = windowState.getStatus();
                    WindowStatus status = WindowStatus.of(statusCode);
                    System.out.println("record>"+windowedUniverseId.getWindow()+"-"+status);
                    switch (status) {
                        case STAGED:
                            this.onStatusStaged(windowedUniverseId, windowState);
                            break;
                        case MERGED:
                            this.onStatusMerged(windowedUniverseId, windowState);
                    }

                });
            }
        } catch (WakeupException var5) {
        } finally {
            this.facade.close();
        }

    }

    public void requestShutdown() {
        this.isShutdownRequested = true;
    }

    private void onStatusStaged(WindowedUniverseId windowedUniverseId, DeltaUniverseState state) {
        //System.out.println("Staged"+state.getMemberDiff().size());
        long universeId = windowedUniverseId.getUniverseId();
        Window window = windowedUniverseId.getWindow();
        if (window.getEnd() < (Long)this.stagedUniverseBoundary.get(universeId)) {
            LOG.debug("{} - u={} skipped sending commits (boundary)", new Supplier[]{() -> {
                return WindowUtil.f(window);
            }, () -> {
                return universeId;
            }});
        } else {
            Collection<Long> producerIds = this.assignedProducers.get(universeId);
            CompletableFuture<Void> allCf = CompletableFuture.allOf((CompletableFuture[])producerIds.stream().map((pId) -> {
                System.out.println(pId+"commit >"+window);
                return this.facade.sendCommit(pId, window, universeId);
            }).toArray((x$0) -> {
                return new CompletableFuture[x$0];
            }));
            allCf.thenAcceptAsync((x) -> {
                LOG.debug("{} - u={} sent all commits", new Supplier[]{() -> {
                    return WindowUtil.f(window);
                }, () -> {
                    return universeId;
                }});
            });
            this.stagedUniverseBoundary.put(universeId, window.getEnd());
        }
    }

    private void onStatusMerged(WindowedUniverseId windowedUniverseId, DeltaUniverseState deltaState) {
        long universeId = windowedUniverseId.getUniverseId();
        Window window = windowedUniverseId.getWindow();
        CompletableFuture<UniverseState> universeChain = (CompletableFuture)this.universeChains.get(universeId);
        universeChain = universeChain.thenApplyAsync((oldState) -> {
            UniverseState state;
            if (oldState.isER()) {
                state = updateUniverseStateER(universeId, window, oldState, deltaState);
            } else {
                state = this.updateUniverseState(universeId, window, oldState, deltaState);
            }

            state.getMembers().forEach((x)->{
                System.out.print(x+" ");
            });
            System.out.println("  >state");

            return state;
        }).exceptionally((e) -> {
            throw new IllegalStateException("failed to update universe state", e);
        });
        this.universeChains.put(universeId, universeChain);
        if (window.getEnd() < (Long)this.mergedUniverseBoundary.get(universeId)) {
            LOG.debug("{} - u={} skipped sending token (boundary)", WindowUtil.f(window), universeId);
        } else {
            Collection<Long> producerIds = this.assignedProducers.get(universeId);
            universeChain.thenApplyAsync((universeState) -> {
                if (universeState.isER()) {
                    if (universeState.isEpochChange()) {
                        Long clearEpoch = universeState.getClearEpoch();
                        if (clearEpoch != null && clearEpoch != -1L) {
                            ((Phaser)universeState.getEpochs().get(clearEpoch)).arrive();
                        }

                        final long newEpoch = universeState.getEpoch();
                        Phaser phaser = new Phaser() {
                            private long epoch = newEpoch;
                            private Collection nodeIds;

                            {
                                this.nodeIds = privacycontroller.this.assignedProducers.get(universeId);
                            }

                            protected boolean onAdvance(int phase, int registeredParties) {
                                ((SecureAggregationNative) privacycontroller.this.secureAggregation.get(universeId)).clearEpochNeighbourhoodsER(this.epoch, this.nodeIds);
                                universeState.getEpochs().remove(this.epoch);
                                return true;
                            }
                        };
                        phaser.register();
                        phaser.register();
                        universeState.getEpochs().put(newEpoch, phaser);
                        ((SecureAggregationNative)this.secureAggregation.get(universeId)).buildEpochNeighbourhoodsER(universeState.getEpoch(), this.assignedProducers.get(universeId), universeState.getMembers(), universeState.getK());
                    } else {
                        ((Phaser)universeState.getEpochs().get(universeState.getEpoch())).register();
                    }
                }

                universeState.getNewMembers().ifPresent((newMembers) -> {
                    if (!newMembers.isEmpty()) {
                        ((SecureAggregationNative)this.secureAggregation.get(universeId)).buildEpochNeighbourhoodsER(universeState.getEpoch(), producerIds, newMembers, universeState.getK());
                    }

                });
                return universeState;
            }).exceptionally((e) -> {
                throw new IllegalStateException("failed ", e);
            }).thenComposeAsync((universeState) -> {
                return CompletableFuture.allOf((CompletableFuture[])producerIds.stream().map((pId) -> {
                    CompletableFuture<PrivacyState> privacyState = (CompletableFuture)this.producerChains.get(pId);
                    privacyState = privacyState.thenApplyAsync((oldState) -> {
                        PrivacyState state = this.updatePrivacyState(pId, oldState, universeState);
                        return state;
                    });
                    this.producerChains.put(pId, privacyState);
                    return privacyState.thenAcceptAsync((state) -> {
                        if(universeState.getMembers().contains(pId)) {
                            Digest token = this.getTransformationToken(pId, universeId, window, universeState, state);
                            this.facade.sendTransformationToken(pId, window, token, universeId);
                        }
                    });
                }).toArray((x$0) -> {
                    return new CompletableFuture[x$0];
                })).thenApply((v) -> {
                    return universeState;
                });
            }).exceptionally((e) -> {
                throw new IllegalStateException("failed to update and build tokens", e);
            }).thenAcceptAsync((universeState) -> {
                ((Phaser)universeState.getEpochs().get(universeState.getEpoch())).arrive();
                LOG.debug("{} - u={} built all tokens", new Supplier[]{() -> {
                    return WindowUtil.f(window);
                }, () -> {
                    return universeId;
                }});
            });
        }
    }

    public static UniverseState updateUniverseStateER(long universeId, Window window, UniverseState old, DeltaUniverseState delta) {
        long windowStart = window.getStart();
        long epoch = WindowUtil.getWindowStart(windowStart, old.getEpochSize());
        short t = (short)((int)((double)(windowStart - epoch) / (double)old.getWindowSize()));
        UniverseState universeState = new UniverseState(epoch, t, old.getUniverseConfig());
        universeState.setEpochs(old.getEpochs());
        boolean newEpoch = epoch != old.getEpoch();
        int codeNew = 1;
        int codeDropped = -1;
        if (newEpoch) {
            Set<Long> members = new HashSet(old.getMembers());
            old.getNewMembersCummulative().ifPresent((newMembersx) -> {
                newMembersx.forEach((pId) -> {
                    members.add(pId);
                });
            });
            old.getDroppedMembersCummulative().ifPresent((droppedMembers) -> {
                droppedMembers.forEach((pId) -> {
                    members.remove(pId);
                });
            });
            delta.getMemberDiff().forEach((pIdStr, codex) -> {
                long pId = Long.parseLong(pIdStr);
                if (codex == codeNew) {
                    members.add(pId);
                } else {
                    if (codex != codeDropped) {
                        throw new IllegalArgumentException("unknown code: " + codex);
                    }

                    members.remove(pId);
                }

            });
            universeState.setMembers(members);
            universeState.markNewEpoch(old.getEpoch());
        } else {
            Set<Long> members = old.getMembers();
            boolean droppedHasChange = false;
            Set<Long> droppedMembersCummulative = (Set)old.getDroppedMembersCummulative().orElse(Collections.emptySet());
            boolean newHasChange = false;
            Set<Long> newMembersCummulative = (Set)old.getNewMembersCummulative().orElse(Collections.emptySet());
            Set<Long> newMembers = new HashSet();
            Iterator<Map.Entry<String, Integer>> iter = delta.getMemberDiff().entrySet().iterator();
            int diffSize = delta.getMemberDiff().size();

            for(int i = 0; i < diffSize; ++i) {
                Map.Entry<String, Integer> e = (Map.Entry)iter.next();
                long pId = Long.parseLong((String)e.getKey());
                long code = (long)(Integer)e.getValue();
                if (code == (long)codeNew && ((Set)droppedMembersCummulative).contains(pId)) {
                    if (!droppedHasChange) {
                        droppedMembersCummulative = new HashSet((Collection)droppedMembersCummulative);
                        droppedHasChange = true;
                    }

                    ((Set)droppedMembersCummulative).remove(pId);
                } else if (code == (long)codeNew) {
                    if (!newHasChange) {
                        newMembersCummulative = new HashSet((Collection)newMembersCummulative);
                        newHasChange = true;
                    }

                    newMembers.add(pId);
                    ((Set)newMembersCummulative).add(pId);
                } else if (code == (long)codeDropped && (members.contains(pId) || ((Set)newMembersCummulative).contains(pId))) {
                    if (!droppedHasChange) {
                        droppedMembersCummulative = new HashSet((Collection)droppedMembersCummulative);
                        droppedHasChange = true;
                    }

                    ((Set)droppedMembersCummulative).add(pId);
                } else {
                    LOG.warn("{} - unhandled member diff p={} code={}", WindowUtil.f(window), pId, code);
                }
            }

            universeState.setMembers(members);
            universeState.setDroppedMembersCummulative((Set)droppedMembersCummulative);
            universeState.setNewMembers(newMembers);
            universeState.setNewMembersCummulative((Set)newMembersCummulative);
        }

        return universeState;
    }

    private UniverseState updateUniverseState(long universeId, Window window, UniverseState old, DeltaUniverseState delta) {
        UniverseState universeState = new UniverseState(old.getUniverseConfig());
        Set<Long> members = new HashSet(old.getMembers());
        int codeNew = 1;
        int codeDropped = -1;
        Map<String, Integer> memberDiff = delta.getMemberDiff();
        memberDiff.forEach((pIdStr, code) -> {
            long pId = Long.parseLong(pIdStr);
            if (code == codeNew) {
                members.add(pId);
            } else {
                if (code != codeDropped) {
                    throw new IllegalArgumentException("unknown status code: " + code);
                }

                members.remove(pId);
            }

        });
        universeState.setMembers(members);
        return universeState;
    }

    private PrivacyState updatePrivacyState(long producerId, PrivacyState old, UniverseState universeState) {
        PrivacyState privacyState = new PrivacyState(old.getPrivacyConfig());
        return privacyState;
    }

    private Digest getTransformationToken(long producerId, long universeId, Window window, UniverseState universeState, PrivacyState privacyState) {
        Digest dummyKeySum;
        Set dropped;
        if (universeState.isER()) {
            dropped = (Set)universeState.getDroppedMembersCummulative().orElse(Collections.emptySet());
            dummyKeySum = ((SecureAggregationNative)this.secureAggregation.get(universeId)).getDummyKeySumER(window.getStart(), universeState.getEpoch(), universeState.getT(), producerId, dropped);
        } else {
            dropped = universeState.getMembers();
            dummyKeySum = ((SecureAggregationNative)this.secureAggregation.get(universeId)).getDummyKeySum(window.getStart(), producerId, dropped);
        }

        Digest heacKey = privacyState.getHeac().getKey(window.getStart() - 1L, window.getEnd() - 1L);
        Digest transformationToken = DigestOp.subtract(dummyKeySum, heacKey);
        System.out.println(producerId+"> token"+transformationToken);
        try {
            Long timestamp= System.currentTimeMillis();
            SQLUtil.statement.executeUpdate(
                    "insert into token values("+timestamp+","+producerId+","+universeId+")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transformationToken;
    }
}
