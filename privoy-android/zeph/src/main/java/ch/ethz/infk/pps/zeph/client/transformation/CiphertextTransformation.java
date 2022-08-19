package ch.ethz.infk.pps.zeph.client.transformation;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.infk.pps.zeph.client.data.CiphertextData;
import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import ch.ethz.infk.pps.shared.avro.ApplicationAdapter;
import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.Input;
import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.crypto.Heac;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;

public class CiphertextTransformation {
	private final long windowSize=10000L;
	private final CiphertextTransformationFacade facade;

	private Heac heac;

	private Map<Long,Long> prevTimestamp;
	private Map<Long,Window> currentWindow;

	private CiphertextTransformation() {
		this.heac = new Heac(MyIdentity.getInstance().getIdentity().getHeacKey());
		this.prevTimestamp=new HashMap<>();
		this.currentWindow=new HashMap<>();
		this.facade=CiphertextTransformationFacade.getInstance();
	}
	public volatile static CiphertextTransformation ciphertextTransformation = null;

	public static CiphertextTransformation getInstance(){
		if (ciphertextTransformation ==null){
			synchronized (CiphertextTransformation.class) {
				if (ciphertextTransformation ==null){
					ciphertextTransformation =new CiphertextTransformation();
				}
			}
		}
		return ciphertextTransformation;
	}

	public boolean checkUniverse(Long universeId){
		return currentWindow.containsKey(universeId);
	}

	public void addUniverse(Universe universe){
		currentWindow.put(universe.getUniverseId(),universe.getFirstWindow());
		prevTimestamp.put(universe.getUniverseId(),universe.getFirstWindow().getStart()-1L);
	}

	public Window startsubmit(long uid) throws InterruptedException {
		facade.startNetty();
		long windowSizeMillis=windowSize;
		long earliestPossibleStart = System.currentTimeMillis();
		long start = WindowUtil.getWindowStart(earliestPossibleStart, windowSizeMillis);
		Window firstWindow = new Window(start + windowSizeMillis, start + 2L * windowSizeMillis);
		Universe universe = new Universe(uid, firstWindow, MyIdentity.getInstance().getMembers(), 8, 0.5, 0.00001);
		addUniverse(universe);
		return firstWindow;
	}
	public void closesubmit(){
		CiphertextTransformationFacade.OPEN=false;
	}


	public void submit(Long universeId,Input value, Long timestamp) {
		if(!checkUniverse(universeId)){
			Log.e("LocalTransformation.submit","no init this universe");
			return;
		}
		this.submitDigest(universeId,ApplicationAdapter.toDigest(value), timestamp);
		//Log.i("LocalTransformation.submit","submit Digest(value) successful");
	}

	public void submitHeartbeat(Long universeId,Long timestamp) {
		if(!checkUniverse(universeId)){
			Log.e("LocalTransformation.submitHeartbeat","no init this universe");
			return;
		}
		this.submitDigest(universeId,(Digest)null, timestamp);
		Log.i("LocalTransformation.submitHeartbeat","submit Digest(null) successful");
	}

	private void submitDigest(Long universeId,Digest digest, Long timestamp) {
		Long prevT=this.prevTimestamp.get(universeId);
		Window currentW=this.currentWindow.get(universeId);
		if (timestamp <= prevT) {
			Log.e("LocalTransformation.submitDigest","prev="+this.prevTimestamp+" cur="+timestamp);
			throw new IllegalArgumentException("cannot submit OoO-records or multiple records with the same timestamp prev=" + this.prevTimestamp + "  timestamp=" + timestamp);
		}else{
			//本次时间戳在当前窗口
			if (timestamp >= currentW.getStart() && timestamp < currentW.getEnd()) {
				if (digest != null) {
					Digest encDigest = this.heac.encrypt(timestamp, digest, prevT);
					facade.sendCiphertext(new CiphertextData(universeId,prevT,timestamp,encDigest));
					//HttpVollyUtil.sendCiphertext(new CiphertextData(universeId,prevT,timestamp,encDigest));
					this.prevTimestamp.put(universeId,timestamp);
					if (timestamp == this.currentWindow.get(universeId).getEnd() - 1L) {
						this.closeCurrentWindowAndMoveToNext(false,universeId);
					}
				}
			}else{
				//当前窗口还是上一次时间戳的窗口，修改当前窗口
				if (prevT >= currentW.getStart() && prevT < currentW.getEnd()) {
					this.closeCurrentWindowAndMoveToNext(true,universeId);
				}
				//打开本次时间戳窗口,并发送
				long start = WindowUtil.getWindowStart(timestamp, this.windowSize);
				this.currentWindow.put(universeId,new Window(start, start + this.windowSize));
				this.prevTimestamp.put(universeId,start - 1L);
				prevT=start - 1L;
				if (digest != null) {
					Digest encDigest = this.heac.encrypt(timestamp, digest, prevT);
					facade.sendCiphertext(new CiphertextData(universeId,prevT,timestamp,encDigest));
					//HttpVollyUtil.sendCiphertext(new CiphertextData(universeId,prevT,timestamp,encDigest));
					this.prevTimestamp.put(universeId,timestamp);
					if (timestamp == this.currentWindow.get(universeId).getEnd() - 1L) {
						this.closeCurrentWindowAndMoveToNext(false,universeId);
					}
				}

			}


		}

	}

	private void closeCurrentWindowAndMoveToNext(boolean submitEmptyBoundaryValue,Long universeId) {
		if (submitEmptyBoundaryValue) {
			Log.d("LocalTransformation.submitDigest.closeCurrentWindowAndMoveToNext", "Submit Empty Boundary Timestamp (End-1)  Window="+this.currentWindow.get(universeId));
			long emptyBoundryTimestamp = this.currentWindow.get(universeId).getEnd() - 1L;
			Digest emptyBoundaryRecord = this.heac.getKey(this.prevTimestamp.get(universeId), emptyBoundryTimestamp);
			facade.sendCiphertext(new CiphertextData(universeId,this.prevTimestamp.get(universeId),emptyBoundryTimestamp,emptyBoundaryRecord));
			//HttpVollyUtil.sendCiphertext(new CiphertextData(universeId,this.prevTimestamp.get(universeId),emptyBoundryTimestamp,emptyBoundaryRecord));
		}
		this.currentWindow.put(universeId, new Window(this.currentWindow.get(universeId).getStart() + this.windowSize,
						                              this.currentWindow.get(universeId).getEnd() + this.windowSize));
		this.prevTimestamp.put(universeId,this.currentWindow.get(universeId).getStart()-1L);
	}


}
