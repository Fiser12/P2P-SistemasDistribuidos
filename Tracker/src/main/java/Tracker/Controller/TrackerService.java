package Tracker.Controller;

import Tracker.VO.TrackerKeepAlive;
import Tracker.View.TrackerWindow;
import Tracker.VO.Tracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by Fiser on 21/10/16.
 */
public class TrackerService {
    TrackerWindow ventana;
    private static TrackerService instance;
    private Tracker tracker;
    private GestorRedundancia gestorRedundancia;

    private TrackerService(){
        ventana = new TrackerWindow();
        tracker = new Tracker();
    }
    public static TrackerService getInstance() {
        if (instance == null) {
            instance = new TrackerService();
        }
        return instance;
    }

    public void ejecutarVentana(){
        ventana.launch();
    }
    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }


    public void connect(String ipAddress, int port, int portForPeers, String id) {
        /*if (id != null && !id.equals("")) {
            gestorRedundancia.setStopListeningPackets(false);
            gestorRedundancia.setStopThreadKeepAlive(false);
            gestorRedundancia.setStopThreadCheckerKeepAlive(false);
        }
        tracker.setId(id);
        tracker.setPort(port);
        tracker.setPortForPeers(portForPeers);
        tracker.setIpAddress(ipAddress);
        tracker.setMaster(false);
        new Thread(gestorRedundancia).start();
        */
    }

    public void disconnect() {
        /*
        gestorRedundancia.setStopListeningPackets(true);
        gestorRedundancia.setStopThreadKeepAlive(true);
        gestorRedundancia.setStopThreadCheckerKeepAlive(true);
        gestorRedundancia.setWaitingToHaveID(true);
        getTracker().getTrackersActivos().clear();
        gestorRedundancia.notifyObservers("DisconnectTracker");
        */
    }
    public List<TrackerKeepAlive> obtenerTrackersActivos() {
        if (getTracker().getTrackersActivos() != null) {
            List<TrackerKeepAlive> listActiveTrackers = new ArrayList<TrackerKeepAlive>();
            Collection<TrackerKeepAlive> activeTrackers = getTracker()
                    .getTrackersActivos().values();
            if (activeTrackers != null) {
                listActiveTrackers.addAll(activeTrackers);
            }
            return listActiveTrackers;
        } else
            return new ArrayList<TrackerKeepAlive>();
    }

    public boolean comprobarIpCorrecta(String ip) {
        String patron = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        return patron.matches(ip);
    }

}
