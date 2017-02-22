package Tracker.Controller;

import Tracker.VO.Tracker;
import Tracker.View.TrackerMain;

import java.util.UUID;

/**
 * Created by Fiser on 21/10/16.
 */
public class TrackerService {
    private TrackerMain ventana;
    private static TrackerService instance;
    private Tracker tracker;
    private GestorRedundancia gestorRedundancia;
    private Thread hiloGestorRedundancia;
    private TrackerService(){
        gestorRedundancia = new GestorRedundancia();
        ventana = new TrackerMain();
        gestorRedundancia.addObserver(ventana);
        tracker = new Tracker();
    }
    public static TrackerService getInstance() {
        if (instance == null) {
            instance = new TrackerService();
        }
        return instance;
    }

    public void ejecutarVentana(){
        ventana.setVisible(true);
    }
    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void connect(String ipAddress, int port, int portForPeers) {
        gestorRedundancia.escuchandoPaquetes = true;
        gestorRedundancia.pararHiloKeepAlive = false;
        gestorRedundancia.pararComprobacionKeepAlive = false;
        tracker.setId(UUID.randomUUID().toString().replace("-", ""));
        tracker.setPort(port);
        tracker.setPortForPeers(portForPeers);
        tracker.setIpAddress(ipAddress);
        tracker.setMaster(false);
        hiloGestorRedundancia = new Thread(gestorRedundancia);
        hiloGestorRedundancia.start();
    }
    public void disconnect(){
        gestorRedundancia.escuchandoPaquetes = false;
        gestorRedundancia.pararHiloKeepAlive = true;
        gestorRedundancia.pararComprobacionKeepAlive = true;
    }
}
