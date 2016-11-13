package Tracker.VO;

import java.util.HashMap;

public class Tracker {

    private String id;
    private String ipAddress;
    private int port;
    private int portForPeers;
    private HashMap<String, TrackerKeepAlive> trackersActivos;
    private boolean master;

    public Tracker() {
    }
    public Tracker(String id, String ipAddress, int port, int portForPeers) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.portForPeers = portForPeers;
    }
    public Tracker(String id,
                   HashMap<String, TrackerKeepAlive> trackersActivos) {
        this.id = id;
        this.trackersActivos = trackersActivos;
    }
    public void addActiveTracker(TrackerKeepAlive activeTracker) {
        trackersActivos.put(activeTracker.getId(), activeTracker);
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPortForPeers() {
        return portForPeers;
    }
    public void setPortForPeers(int portForPeers) {
        this.portForPeers = portForPeers;
    }
    public HashMap<String, TrackerKeepAlive> getTrackersActivos() {
        if (trackersActivos == null) {
            trackersActivos = new HashMap<String, TrackerKeepAlive>();
        }
        return trackersActivos;
    }
    public void setTrackersActivos(HashMap<String, TrackerKeepAlive> trackersActivos) {
        this.trackersActivos = trackersActivos;
    }
    public boolean isMaster() {
        return master;
    }
    public void setMaster(boolean master) {
        this.master = master;
    }

}

