package data;


import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.zeph.shared.DigestOp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CiphertextData {
    private long produceId;
    private long universeId;
    private long prevTimestamp;
    private long timestamp;
    private List<Long> data;

    public CiphertextData(){ }

    public Long getProduceId(){
        return produceId;
    }

    public void setProduceId(long produceId) {
        this.produceId = produceId;
    }

    public long getUniverseId() {
        return universeId;
    }

    public void setUniverseId(long universeId) {
        this.universeId = universeId;
    }

    public long getPrevTimestamp() {
        return prevTimestamp;
    }

    public void setPrevTimestamp(long prevTimestamp) {
        this.prevTimestamp = prevTimestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }

    public static Digest toDigest(List<Long> list){
        if(list==null || list.size()!=3) return null;
        long[] datalong=list.stream().mapToLong(t->t.longValue()).toArray();
        return DigestOp.of(datalong);
    }

    public static List<Long> fromDigest(Digest digest){
        if(digest==null) return new ArrayList<Long>();
        List<Long> datalong=digest.getValueBase();
        datalong.addAll(digest.getCount());
        return datalong;
    }

    public String toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {

            jsonObject.put("produceId", getProduceId());
            jsonObject.put("universeId", getUniverseId());
            jsonObject.put("prevTimestamp", getPrevTimestamp());
            jsonObject.put("timestamp", getTimestamp());
            jsonObject.put("data", toDigest(getData()));

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";

        }
    }


}


