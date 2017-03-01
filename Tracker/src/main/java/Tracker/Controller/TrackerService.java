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
    private UDPManager udpServer;
    private TrackerService(){
        gestorRedundancia = new GestorRedundancia();
        udpServer = UDPManager.getInstance();
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
    public TrackerMain getVentana() {
        return ventana;
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

    public void connect(String ipAddress, int portForPeers) {
        gestorRedundancia.escuchandoPaquetes = true;
        gestorRedundancia.pararHiloKeepAlive = false;
        gestorRedundancia.pararComprobacionKeepAlive = false;
        tracker.setId(UUID.randomUUID().toString().replace("-", ""));
        tracker.setPortForPeers(portForPeers);
        tracker.setIpAddress(ipAddress);
        tracker.setMaster(false);
        udpServer.start();
        hiloGestorRedundancia = new Thread(gestorRedundancia);
        hiloGestorRedundancia.start();
    }
    public void disconnect(){
        gestorRedundancia.escuchandoPaquetes = false;
        gestorRedundancia.pararHiloKeepAlive = true;
        gestorRedundancia.pararComprobacionKeepAlive = true;
    }
}
