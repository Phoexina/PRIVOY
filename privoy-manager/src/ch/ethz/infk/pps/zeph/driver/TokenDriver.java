package ch.ethz.infk.pps.zeph.driver;

import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.client.PrivacyController;
import ch.ethz.infk.pps.zeph.client.facade.PrivacyControllerFacade;
import ch.ethz.infk.pps.zeph.client.loader.FileLoader;
import ch.ethz.infk.pps.zeph.client.util.ClientConfig;
import ch.ethz.infk.pps.zeph.client.util.LoaderUtil;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.ProducerIdentity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenDriver implements Runnable{

    private privacycontroller privacyController;
    private FileLoader fileLoader=FileLoader.getInstance();
    private List<Long> universeIds;
    public TokenDriver(List<Long> universeIds){
        this.universeIds=universeIds;
    }
    private boolean shutdownRequested = false;

    @Override
    public void run() {
        List<Long> members=fileLoader.toLongs();
        Map<Long, Universe> universes=new HashMap<>();
        List<ClientConfig> clientConfigs= new ArrayList<>();

        long windowSizeMillis=10000;
        long earliestPossibleStart = System.currentTimeMillis();
        long start = WindowUtil.getWindowStart(earliestPossibleStart, windowSizeMillis);
        Window firstWindow = new Window(start + windowSizeMillis, start + 2L * windowSizeMillis);

        for(Long universeId:universeIds){
            clientConfigs.addAll(fileLoader.toClientConfigs(universeId));
            Universe universe = new Universe(universeId, firstWindow,members, 8, 0.5, 0.00001);
            universes.put(universeId,universe);
        }
        PrivacyControllerFacade facade=new PrivacyControllerFacade(2L,"8.130.10.212:9092", Duration.ofMillis(3000));
        privacyController=new privacycontroller(clientConfigs,universes,facade);
        System.out.println("create PrivacyController successfully");
        privacyController.run();

    }
    public void addProducerConfig(Long pId, ProducerIdentity pInfo){
        for(Long universeId:universeIds){
            ClientConfig config = new ClientConfig();
            config.setProducerId(pId);
            config.setUniverseId(universeId);
            config.setHeacKey(pInfo.getHeacKey());
            config.setPrivateKey(pInfo.getPrivateKey());
            config.setSharedKeys(LoaderUtil.getProducerSharedKeys(pId, pInfo));
            privacyController.addAssignment(universeId,config);
        }


    }

    public void requestShutdown() {
        this.shutdownRequested = true;
    }
    protected boolean isShutdownRequested() {
        return this.shutdownRequested;
    }
}
