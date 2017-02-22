package Tracker.VO;

public class Tracker {
    private String id;
    private String ipAddress;
    private int port;
    private int portForPeers;
    private boolean master;

    public Tracker() {
    }
    public Tracker(String id, String ipAddress, int port, int portForPeers) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.portForPeers = portForPeers;
    }
    public Tracker(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setPortForPeers(int portForPeers) {
        this.portForPeers = portForPeers;
    }
    public boolean isMaster() {
        return master;
    }
    public void setMaster(boolean master) {
        this.master = master;
    }
}

