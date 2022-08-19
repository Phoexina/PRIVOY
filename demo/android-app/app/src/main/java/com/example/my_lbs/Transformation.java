package com.example.my_lbs;

import android.util.Log;
import ch.ethz.infk.pps.shared.avro.ApplicationAdapter;
import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.Input;
import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.client.data.CiphertextData;
import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import ch.ethz.infk.pps.zeph.client.transformation.CiphertextTransformationFacade;
import ch.ethz.infk.pps.zeph.crypto.Heac;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Transformation {
    private final long windowSize = 10000L;
    private final CiphertextTransformationFacade facade = CiphertextTransformationFacade.getInstance();
    private Heac heac = new Heac(MyIdentity.getInstance().getIdentity().getHeacKey());
    private Map<Long, Long> prevTimestamp = new HashMap();
    private Map<Long, Window> currentWindow = new HashMap();
    public static volatile Transformation ciphertextTransformation = null;

    private Transformation() throws InterruptedException {
    }

    public static Transformation getInstance() throws InterruptedException {
        if (ciphertextTransformation == null) {
            Class var0 = Transformation.class;
            synchronized(Transformation.class) {
                if (ciphertextTransformation == null) {
                    ciphertextTransformation = new Transformation();
                }
            }
        }

        return ciphertextTransformation;
    }

    public boolean checkUniverse(Long universeId) {
        return this.currentWindow.containsKey(universeId);
    }

    public void addUniverse(Universe universe) {
        this.currentWindow.put(universe.getUniverseId(), universe.getFirstWindow());
        this.prevTimestamp.put(universe.getUniverseId(), universe.getFirstWindow().getStart() - 1L);
    }

    public void submit(Long universeId, Input value, Long timestamp) {
        if (!this.checkUniverse(universeId)) {
            Log.e("LocalTransformation.submit", "no init this universe");
        } else {
            this.submitDigest(universeId, ApplicationAdapter.toDigest(value), timestamp);
        }
    }

    public void submitHeartbeat(Long universeId, Long timestamp) {
        if (!this.checkUniverse(universeId)) {
            Log.e("LocalTransformation.submitHeartbeat", "no init this universe");
        } else {
            this.submitDigest(universeId, (Digest)null, timestamp);
            Log.i("LocalTransformation.submitHeartbeat", "submit Digest(null) successful");
        }
    }

    private void submitDigest(Long universeId, Digest digest, Long timestamp) {
        Long prevT = (Long)this.prevTimestamp.get(universeId);
        Window currentW = (Window)this.currentWindow.get(universeId);
        if (timestamp <= prevT) {
            Log.e("LocalTransformation.submitDigest", "prev=" + this.prevTimestamp + " cur=" + timestamp);
            throw new IllegalArgumentException("cannot submit OoO-records or multiple records with the same timestamp prev=" + this.prevTimestamp + "  timestamp=" + timestamp);
        } else {
            if (timestamp >= currentW.getStart() && timestamp < currentW.getEnd()) {
                if (digest != null) {
                    Digest encDigest = this.heac.encrypt(timestamp, digest, prevT);
                    this.facade.sendCiphertext(new CiphertextData(universeId, prevT, timestamp, encDigest));
                    this.prevTimestamp.put(universeId, timestamp);
                    if (timestamp == ((Window)this.currentWindow.get(universeId)).getEnd() - 1L) {
                        this.closeCurrentWindowAndMoveToNext(false, universeId);
                    }
                }
            } else {
                if (prevT >= currentW.getStart() && prevT < currentW.getEnd()) {
                    this.closeCurrentWindowAndMoveToNext(true, universeId);
                }

                long var10000 = timestamp;
                Objects.requireNonNull(this);
                long start = WindowUtil.getWindowStart(var10000, 10000L);
                Map var9 = this.currentWindow;
                Long var10004 = start;
                Objects.requireNonNull(this);
                var9.put(universeId, new Window(var10004, start + 10000L));
                this.prevTimestamp.put(universeId, start - 1L);
                if (digest != null) {
                    Digest encDigest = this.heac.encrypt(timestamp, digest, prevT);
                    this.facade.sendCiphertext(new CiphertextData(universeId, prevT, timestamp, encDigest));
                    this.prevTimestamp.put(universeId, timestamp);
                    if (timestamp == ((Window)this.currentWindow.get(universeId)).getEnd() - 1L) {
                        this.closeCurrentWindowAndMoveToNext(false, universeId);
                    }
                }
            }

        }
    }

    private void closeCurrentWindowAndMoveToNext(boolean submitEmptyBoundaryValue, Long universeId) {
        if (submitEmptyBoundaryValue) {
            Log.d("LocalTransformation.submitDigest.closeCurrentWindowAndMoveToNext", "Submit Empty Boundary Timestamp (End-1)  Window=" + this.currentWindow.get(universeId));
            long emptyBoundryTimestamp = ((Window)this.currentWindow.get(universeId)).getEnd() - 1L;
            Digest emptyBoundaryRecord = this.heac.getKey((Long)this.prevTimestamp.get(universeId), emptyBoundryTimestamp);
            this.facade.sendCiphertext(new CiphertextData(universeId, (Long)this.prevTimestamp.get(universeId), emptyBoundryTimestamp, emptyBoundaryRecord));
        }

        Map var10000 = this.currentWindow;
        long var10004 = ((Window)this.currentWindow.get(universeId)).getStart();
        Objects.requireNonNull(this);
        Long var6 = var10004 + 10000L;
        long var10005 = ((Window)this.currentWindow.get(universeId)).getEnd();
        Objects.requireNonNull(this);
        var10000.put(universeId, new Window(var6, var10005 + 10000L));
        this.prevTimestamp.put(universeId, ((Window)this.currentWindow.get(universeId)).getStart() - 1L);
    }
}
