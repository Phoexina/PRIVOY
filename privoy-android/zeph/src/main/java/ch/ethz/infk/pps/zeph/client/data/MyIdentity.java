package ch.ethz.infk.pps.zeph.client.data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import ch.ethz.infk.pps.zeph.shared.pojo.ImmutableUnorderedPair;
import ch.ethz.infk.pps.zeph.shared.pojo.ProducerIdentity;

public class MyIdentity {
    private Long id;
    private ProducerIdentity identity;
    private Map<Long, SecretKey> saveSharedKeys;
    private Map<ImmutableUnorderedPair<Long>, SecretKey> sharedKeys;

    private MyIdentity(){
        id=-1L;
        saveSharedKeys=new ConcurrentHashMap();
    }

    public volatile static MyIdentity myIdentity = null;

    public static MyIdentity getInstance() {
        if (myIdentity==null){
            synchronized (MyIdentity.class) {
                if (myIdentity==null){
                    myIdentity=new MyIdentity();
                }
            }
        }
        return myIdentity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProducerIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(ProducerIdentity identity) {
        this.identity = identity;
    }

    public Map<Long, SecretKey> getSaveSharedKeys() {
        return saveSharedKeys;
    }
    public void setSaveSharedKeys(Map<Long, SecretKey> saveSharedKeys) {
        this.saveSharedKeys = saveSharedKeys;
    }

    public List<Long> getMembers(){
        return saveSharedKeys.keySet().stream().collect(Collectors.toList());
    }

    public void flushSharedKeys() {
        //saveSharedKeys.remove(id);
        sharedKeys=(Map)saveSharedKeys.entrySet().stream().collect(Collectors.toMap((e) -> {
            if(!e.getKey().equals(MyIdentity.getInstance().getId()))
                return ImmutableUnorderedPair.of((Long)e.getKey(),MyIdentity.getInstance().getId());
            else
                return null;
        }, (e) -> {
            return (SecretKey)e.getValue();
        }));
    }

    public Map<ImmutableUnorderedPair<Long>, SecretKey> getSharedKeys() {
        return sharedKeys;
    }
}
