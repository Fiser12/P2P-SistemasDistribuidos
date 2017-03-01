package Tracker.Util.bittorrent.tracker.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Offset      Size            	Name            Value
 * 0           32-bit integer  	action          1 // announce
 * 4           32-bit integer  	transaction_id
 * 8           32-bit integer  	interval
 * 12          32-bit integer  	leechers
 * 16          32-bit integer  	seeders
 * 20 + 6 * n  32-bit integer  	IP address
 * 24 + 6 * n  16-bit integer  	TCP port
 * 20 + 6 * N
 */

public class AnnounceResponse extends BitTorrentUDPMessage {

    private int interval;
    private int leechers;
    private int seeders;

    private List<PeerInfo> peers;

    public AnnounceResponse() {
        super(Action.ANNOUNCE);
        this.peers = new ArrayList<>();
    }


    @Override
    public byte[] getBytes() {
        int size = 20 + 6 * peers.size();

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(0, getAction().value());
        byteBuffer.putInt(4, getTransactionId());
        byteBuffer.putInt(8, getInterval());
        byteBuffer.putInt(12, getLeechers());
        byteBuffer.putInt(16, getSeeders());

        int i = 20;
        for (PeerInfo p : peers) {
            int port = p.getPort();
            int ip = p.getIpAddress();
            byteBuffer.putInt(i, ip);
            byteBuffer.putShort(i + 4, (short) port);
            i += 6;
        }
        byteBuffer.flip();
        return byteBuffer.array();
    }
    public static AnnounceResponse parse(byte[] byteArray) {
        int initialSize = 20;
        int peerSize = 6;

        try {
            ByteBuffer buffer = ByteBuffer.wrap(byteArray);
            buffer.order(ByteOrder.BIG_ENDIAN);

            AnnounceResponse msg = new AnnounceResponse();

            msg.setAction(Action.valueOf(buffer.getInt(0)));
            msg.setTransactionId(buffer.getInt(4));
            msg.setInterval(buffer.getInt(8));
            msg.setLeechers(buffer.getInt(12));
            msg.setSeeders(buffer.getInt(16));

            int index = initialSize;
            PeerInfo peerInfo;

            while ((index + peerSize) < byteArray.length) {
                peerInfo = new PeerInfo();
                peerInfo.setIpAddress(buffer.getInt(index));
                peerInfo.setPort(buffer.getShort(index + 4));
                msg.getPeers().add(peerInfo);
                index += peerSize;
            }

            return msg;
        } catch (Exception ex) {
            System.out.println("# Error parsing AnnounceResponse message: " + ex.getMessage());
        }

        return null;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getLeechers() {
        return leechers;
    }

    public void setLeechers(int leechers) {
        this.leechers = leechers;
    }

    public int getSeeders() {
        return seeders;
    }

    public void setSeeders(int seeders) {
        this.seeders = seeders;
    }

    public List<PeerInfo> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerInfo> peers) {
        this.peers = peers;
    }
}
