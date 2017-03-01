package Tracker.Util.bittorrent.tracker.protocol.udp;

import Tracker.Util.bittorrent.util.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Offset  Size    			Name    			Value
 * 0       64-bit integer  	connection_id
 * 8       32-bit integer  	action          	1 // announce
 * 12      32-bit integer  	transaction_id
 * 16      20-byte string  	info_hash
 * 36      20-byte string  	peer_id
 * 56      64-bit integer  	downloaded
 * 64      64-bit integer  	left
 * 72      64-bit integer  	uploaded
 * 80      32-bit integer  	event           	0 // 0: none; 1: completed; 2: started; 3: stopped
 * 84      32-bit integer  	IP address      	0 // default
 * 88      32-bit integer  	key
 * 92      32-bit integer  	num_want        	-1 // default
 * 96      16-bit integer  	port
 * 98
 */

public class AnnounceRequest extends BitTorrentUDPRequestMessage {

    private byte[] infoHash;
    private String peerId;
    private long downloaded;
    private long left;
    private long uploaded;
    private Event event;
    private int key;
    private int numWant = -1;
    private PeerInfo peerInfo;

    public AnnounceRequest() {
        super(Action.ANNOUNCE);

        this.peerInfo = new PeerInfo();
        this.peerInfo.setIpAddress(0);
    }

    public static AnnounceRequest parse(byte[] byteArray) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
            AnnounceRequest announceRequest = new AnnounceRequest();

            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            byte[] hash = new byte[20];
            byte[] idPeer = new byte[20];
            announceRequest.setConnectionId(byteBuffer.getLong(0));
            announceRequest.setAction(Action.valueOf(byteBuffer.getInt(8)));
            announceRequest.setTransactionId(byteBuffer.getInt(12));
            byteBuffer.position(16);

            byteBuffer.get(hash);
            announceRequest.setInfoHash(hash);

            byteBuffer.position(36);
            byteBuffer.get(idPeer);

            announceRequest.setPeerId( new String( idPeer ) );
            announceRequest.setDownloaded(byteBuffer.getLong(56));
            announceRequest.setLeft(byteBuffer.getLong(64));
            announceRequest.setUploaded(byteBuffer.getLong(72));
            announceRequest.setEvent(Event.values()[(byteBuffer.getInt(80))]);

            PeerInfo peerInfo = new PeerInfo();
            peerInfo.setIpAddress(byteBuffer.getInt(84));
            peerInfo.setPort(byteBuffer.getShort(96));
            announceRequest.setPeerInfo(peerInfo);
            announceRequest.setKey(byteBuffer.getInt(88));
            announceRequest.setNumWant(byteBuffer.getInt(92));

            return announceRequest;
        } catch (Exception ex) {
            System.out.println("# ERROR PARSE ANNOUNCE REQUEST " + ex.getMessage());
        }
        return null;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(98);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putLong(0, super.getConnectionId());
        buffer.putInt(8, super.getAction().value());
        buffer.putInt(12, super.getTransactionId());
        buffer.position(16);
        buffer.put(this.infoHash);
        buffer.position(36);
        buffer.put(this.peerId.getBytes());
        buffer.putLong(56, this.downloaded);
        buffer.putLong(64, this.left);
        buffer.putLong(72, this.uploaded);
        buffer.putInt(80, event.value);
        buffer.putInt(84, this.peerInfo.getIpAddress());
        buffer.putInt(88, this.key);
        buffer.putInt(92, this.numWant);
        buffer.putShort(96, (short) this.peerInfo.getPort());

        buffer.flip();

        return buffer.array();
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(byte[] infoHash) {
        this.infoHash = infoHash;
    }

    public String getHexInfoHash() {
        return ByteUtils.toHexString(this.infoHash);
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public long getLeft() {
        return left;
    }

    public void setLeft(long left) {
        this.left = left;
    }

    public long getUploaded() {
        return uploaded;
    }

    public void setUploaded(long uploaded) {
        this.uploaded = uploaded;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getNumWant() {
        return numWant;
    }

    public void setNumWant(int numWant) {
        this.numWant = numWant;
    }

    public PeerInfo getPeerInfo() {
        return peerInfo;
    }

    public void setPeerInfo(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
    }

    public enum Event {
        NONE(0),
        COMPLETED(1),
        STARTED(2),
        STOPPED(3);

        private int value;

        Event(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}