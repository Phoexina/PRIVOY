package kstream;

import ch.ethz.infk.pps.shared.avro.DeltaUniverseState;
import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.HeacHeader;
import ch.ethz.infk.pps.shared.avro.Membership;
import ch.ethz.infk.pps.shared.avro.Token;
import ch.ethz.infk.pps.shared.avro.TransformationResult;
import ch.ethz.infk.pps.shared.avro.UniversePartitionStatus;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.server.master.processor.CommittingPhasePunctuator;
import ch.ethz.infk.pps.zeph.shared.DigestOp;
import ch.ethz.infk.pps.zeph.shared.TransformationResultOp;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowStatus;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowedUniverseId;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.processor.To;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.util.Supplier;

public class statushandler {
    private static final Logger LOG = LogManager.getLogger();
    private Marker m;
    private final int statusOpen;
    private final int statusStaged;
    private final int statusCommitted;
    private final int statusMerged;
    private final int statusClosed;
    private final ProcessorContext ctx;
    private final Duration timeForCommit;
    private final KeyValueStore<WindowedUniverseId, Integer> statusStore;
    private final KeyValueStore<WindowedUniverseId, UniversePartitionStatus> taskStatusStore;
    private final KeyValueStore<WindowedUniverseId, Digest> resultStore;
    private final KeyValueStore<WindowedUniverseId, Membership> membershipDiffStore;
    private Map<Window, Cancellable> cancellableMap;
    private static final String NO_RESULT_MARKER = "noresult";

    public statushandler(ProcessorContext ctx, Duration timeForCommit) {
        this.statusOpen = WindowStatus.OPEN.code();
        this.statusStaged = WindowStatus.STAGED.code();
        this.statusCommitted = WindowStatus.COMMITTED.code();
        this.statusMerged = WindowStatus.MERGED.code();
        this.statusClosed = WindowStatus.CLOSED.code();
        this.cancellableMap = new HashMap();
        this.ctx = ctx;
        this.timeForCommit = timeForCommit;
        this.statusStore = (KeyValueStore)this.ctx.getStateStore("status-store");
        this.taskStatusStore = (KeyValueStore)this.ctx.getStateStore("task-status-store");
        this.resultStore = (KeyValueStore)this.ctx.getStateStore("result-store");
        this.membershipDiffStore = (KeyValueStore)this.ctx.getStateStore("membership-store");
        this.m = MarkerManager.getMarker("status_change_handler");
    }

    public void handleStatusOpen(Long universeId, String taskId, Window window) {
        LOG.debug(this.m, "{} - universe={} task={} - status open", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return universeId;
        }, () -> {
            return taskId;
        }});
        WindowedUniverseId key = new WindowedUniverseId(universeId, window);
        UniversePartitionStatus record = (UniversePartitionStatus)this.taskStatusStore.get(key);
        if (record == null) {
            record = new UniversePartitionStatus(new HashMap());
            this.statusStore.put(key, this.statusOpen);
            LOG.info(this.m, "{} - universe={} - status open", new Supplier[]{() -> {
                return WindowUtil.f(window);
            }, () -> {
                return universeId;
            }});
            this.ctx.forward(key, new DeltaUniverseState(this.statusOpen, (Map)null, (Digest)null), To.child("SINK.u-info"));
        }

        Integer taskStatus = (Integer)record.getTaskStatus().get(taskId);
        if (taskStatus != null && taskStatus != this.statusOpen) {
            throw new IllegalStateException("open: task status must be null or open");
        } else {
            record.getTaskStatus().put(taskId, this.statusOpen);
            this.taskStatusStore.put(key, record);
        }
    }

    public void handleStatusStaged(long universeId, String taskId, Window window) {
        LOG.debug(this.m, "{} - universe={} task={} - status staged", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return universeId;
        }, () -> {
            return taskId;
        }});
        WindowedUniverseId key = new WindowedUniverseId(universeId, window);
        UniversePartitionStatus record = (UniversePartitionStatus)this.taskStatusStore.get(key);
        Integer taskStatus = (Integer)record.getTaskStatus().get(taskId);
        if (taskStatus != this.statusOpen && taskStatus != this.statusStaged) {
            throw new IllegalStateException("staged: task status must be open or staged");
        } else {
            if (taskStatus == this.statusOpen) {
                record.getTaskStatus().put(taskId, this.statusStaged);
                this.taskStatusStore.put(key, record);
                System.out.println("handleStatusStaged put:"+key.getWindow()+"-"+this.taskStatusStore.get(key));
                boolean hasOpenTask = record.getTaskStatus().containsValue(this.statusOpen);
                boolean hasAllTasks = true;
                if (!hasOpenTask && hasAllTasks) {
                    this.startCommittingPhase(key);
                }
            }

        }
    }

    private void startCommittingPhase(WindowedUniverseId key) {
        Cancellable c = this.ctx.schedule(this.timeForCommit, PunctuationType.WALL_CLOCK_TIME, new committingphase(this, this.cancellableMap, key.getWindow(), key.getUniverseId(), this.m));
        this.cancellableMap.put(key.getWindow(), c);
        LOG.info(this.m, "{} - universe={} - status staged", new Supplier[]{() -> {
            return WindowUtil.f(key.getWindow());
        }, () -> {
            return key.getUniverseId();
        }});
        this.statusStore.put(key, this.statusStaged);
        //System.out.println("startCommittingPhase put:"+key+"-"+(UniversePartitionStatus)this.taskStatusStore.get(key));
        this.ctx.forward(key, new DeltaUniverseState(this.statusStaged, (Map)null, (Digest)null), To.child("SINK.u-info"));
    }

    public void handleStatusCommitted(long universeId, String taskId, Window window) {
        LOG.debug(this.m, "{} - universe={} task={} - status committed", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return universeId;
        }, () -> {
            return taskId;
        }});
        WindowedUniverseId key = new WindowedUniverseId(universeId, window);
        UniversePartitionStatus record = (UniversePartitionStatus)this.taskStatusStore.get(key);
        System.out.println("handleStatusCommitted:"+key.getWindow()+"-"+record);
        if(record==null) return;
        Integer taskStatus = (Integer)record.getTaskStatus().get(taskId);
        if(taskStatus==null) return;
        if (taskStatus != this.statusStaged && taskStatus != this.statusCommitted) {
            LOG.warn(this.m, "{} - universe={} task={} committed: task status must be staged or committed: taskStatus={}", new Supplier[]{() -> {
                return WindowUtil.f(window);
            }, () -> {
                return universeId;
            }, () -> {
                return taskId;
            }, () -> {
                return taskStatus;
            }});
            System.out.println("committed: task status must be staged or committed");
        } else {
            if (taskStatus == this.statusStaged) {
                System.out.println("taskStatus == this.statusStaged");
                record.getTaskStatus().put(taskId, this.statusCommitted);
                boolean hasStagedTask = record.getTaskStatus().containsValue(this.statusStaged);
                if (!hasStagedTask) {
                    Cancellable c = (Cancellable)this.cancellableMap.remove(window);
                    if(c==null) return;
                    c.cancel();
                    this.handleUniverseStatusCommitted(universeId, window);
                } else {
                    this.taskStatusStore.put(key, record);
                }
            }

        }
    }

    protected void handleUniverseStatusCommitted(long universeId, Window window) {
        LOG.info(this.m, "{} - universe={} - status committed", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return universeId;
        }});
        WindowedUniverseId key = new WindowedUniverseId(universeId, window);
        UniversePartitionStatus record = (UniversePartitionStatus)this.taskStatusStore.get(key);
        record.getTaskStatus().replaceAll((taskId, oldStatus) -> {
            if (oldStatus != this.statusStaged && oldStatus != this.statusCommitted) {
                LOG.error(this.m, "{} - failed handle status committed -> not all status were staged or committed  [old status = {}]", WindowUtil.f(window), oldStatus);
            }

            return this.statusCommitted;
        });
        //向u-tokens发送pid为0的commit window
        Membership membership = new Membership(new HashMap());
        this.membershipDiffStore.put(key, membership);
        Token committedToken = new Token(window, "" + universeId, (Digest)null, (HeacHeader)null, this.statusCommitted);
        To to = To.child("SINK.u-tokens");
        record.getTaskStatus().keySet().stream().map((taskIdStr) -> {
            return TaskId.parse(taskIdStr);
        }).forEach((taskId) -> {
            this.ctx.forward((long)taskId.partition, committedToken, to);
            System.out.println("0 token"+committedToken);
        });
        this.taskStatusStore.put(key, record);
        this.statusStore.put(key, this.statusCommitted);
        this.ctx.forward(key, new DeltaUniverseState(this.statusCommitted, (Map)null, (Digest)null), To.child("SINK.u-info"));
    }

    public void handleStatusMerged(long universeId, String taskId, Window window, Map<String, Integer> diffs) {
        Integer marker = (Integer)diffs.remove("noresult");
        boolean taskSendsResult = marker == null;
        LOG.debug(this.m, "{} - universe={} task={} - status merged [send result = {}] [diffs = {}]", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return universeId;
        }, () -> {
            return taskId;
        }, () -> {
            return taskSendsResult;
        }, () -> {
            return diffs;
        }});
        WindowedUniverseId key = new WindowedUniverseId(universeId, window);
        UniversePartitionStatus record = (UniversePartitionStatus)this.taskStatusStore.get(key);
        Integer taskStatus = (Integer)record.getTaskStatus().get(taskId);
        if (taskStatus != this.statusCommitted) {
            throw new IllegalStateException("merged: task status must be committed");
        } else {
            if (taskSendsResult) {
                record.getTaskStatus().put(taskId, this.statusMerged);
            } else {
                record.getTaskStatus().remove(taskId);
            }

            this.taskStatusStore.put(key, record);
            Membership membership = (Membership)this.membershipDiffStore.get(key);
            System.out.println("membership"+membership);
            membership.getDiff().putAll(diffs);
            System.out.println("new membership"+membership);
            this.membershipDiffStore.put(key, membership);
            boolean hasUnmergedTask = record.getTaskStatus().containsValue(this.statusCommitted);
            if (!hasUnmergedTask) {
                LOG.info(this.m, "{} - universe={} - status merged   (membership-diff={})", new Supplier[]{() -> {
                    return WindowUtil.f(window);
                }, () -> {
                    return universeId;
                }, () -> {
                    return membership;
                }});
                this.statusStore.put(key, this.statusMerged);
                this.ctx.forward(key, new DeltaUniverseState(this.statusMerged, membership.getDiff(), (Digest)null), To.child("SINK.u-info"));
            }

        }
    }

    public void handleStatusClosed(long universeId, String taskId, Window window, Digest taskResult) {
        LOG.debug(this.m, "{} - universe={} task={} - status closed (task result = {})", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return universeId;
        }, () -> {
            return taskId;
        }, () -> {
            return taskResult;
        }});
        WindowedUniverseId key = new WindowedUniverseId(universeId, window);
        UniversePartitionStatus record = (UniversePartitionStatus)this.taskStatusStore.get(key);
        Integer taskStatus = (Integer)record.getTaskStatus().remove(taskId);
        if (taskStatus != this.statusMerged) {
            throw new IllegalStateException("closed: task status must be merged");
        } else {
            Digest aggregator = (Digest)this.resultStore.get(key);
            if (aggregator == null) {
                aggregator = DigestOp.empty();
            }

            aggregator = DigestOp.add(aggregator, taskResult);
            boolean hasAllTaskResult = record.getTaskStatus().isEmpty();
            if (hasAllTaskResult) {
                this.taskStatusStore.delete(key);
                this.statusStore.put(key, this.statusClosed);
                this.resultStore.delete(key);
                LOG.info(this.m, "{} - universe={} - status closed - result={}  delay={}", WindowUtil.f(window), universeId, aggregator, Duration.ofMillis(System.currentTimeMillis() - window.getEnd()));
                TransformationResult result = TransformationResultOp.of(aggregator, window);
                long timestamp = System.currentTimeMillis();
                this.ctx.forward(universeId, result, To.child("SINK.results").withTimestamp(timestamp));
                this.ctx.forward(key, new DeltaUniverseState(this.statusClosed, (Map)null, aggregator), To.child("SINK.u-info"));
            } else {
                this.taskStatusStore.put(key, record);
                this.resultStore.put(key, aggregator);
            }

        }
    }
}
