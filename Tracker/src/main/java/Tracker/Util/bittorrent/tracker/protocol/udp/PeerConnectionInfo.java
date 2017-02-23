package Tracker.Util.bittorrent.tracker.protocol.udp;

/**
 * Size				Name
 * 32-bit integer  	IP address
 * 16-bit integer  	TCP port
 */

public class PeerConnectionInfo {
    private int ipAddress;
    private short port;

    public int getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(int ipAddress) {
        this.ipAddress = ipAddress;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("ip: ");
        buffer.append(this.ipAddress);
        buffer.append(" - port: ");
        buffer.append(this.port);

        return buffer.toString();
    }
}