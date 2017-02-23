package Tracker.Util.bittorrent.peer.protocol;

/**
 * handshake: <pstrlen><pstr><reserved><info_hash><peer_id>
 * <p>
 * The handshake is a required message and must be the first message
 * transmitted by the client. It is (49+len(pstr)) bytes long.
 * - pstrlen: string length of <pstr>, as a single raw byte
 * - pstr: string identifier of the protocol
 * - reserved: eight (8) reserved bytes. All current implementations use all zeroes.
 * - info_hash: 20-byte SHA1 hash of the info key in the metainfo file.
 * This is the same info_hash that is transmitted in tracker requests.
 * - peer_id: 20-byte string used as a unique ID for the client.
 * This is usually the same peer_id that is transmitted in tracker requests.
 * <p>
 * In version 1.0 of the BitTorrent protocol, pstrlen = 19, and pstr = "BitTorrent protocol".
 */

import Tracker.Util.bittorrent.util.ByteUtils;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class Handsake {
    private static final String DEFAULT_PROTOCOL = "BitTorrent protocol";
    private static final String RESERVED = "00000000";

    private int nameLength;
    private String protocolName;
    private String reserved;
    private byte[] infoHash;
    private String peerId;

    public Handsake() {
        this.protocolName = Handsake.DEFAULT_PROTOCOL;
        this.nameLength = this.protocolName.length();
        this.reserved = Handsake.RESERVED;
    }

    public int getNameLength() {
        return nameLength;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
        this.nameLength = protocolName.length();
    }

    public String getReserved() {
        return reserved;
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(byte[] infoHash) {
        this.infoHash = infoHash;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            result.write(this.nameLength);
            result.write(this.protocolName.getBytes());
            result.write(this.reserved.getBytes());
            result.write(this.infoHash);
            result.write(this.peerId.getBytes());

            return result.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }
    public static Handsake parseHandsake(byte[] bytes) {
        //2017-01-22 BUG FIXED
        // - Check the length of the byte array
        // - +1 in the second index of the copyOfRenge() method
        if (bytes.length == 68) {
            Handsake handsake = new Handsake();
            handsake.setInfoHash(Arrays.copyOfRange(bytes, 28, 48));
            handsake.setPeerId(DatatypeConverter.printBase64Binary(Arrays.copyOfRange(bytes, 48, 68)));

            return handsake;
        } else {
            return null;
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("NAME_LENGTH: '");
        buffer.append(this.nameLength);
        buffer.append("' - PROTOCOL_NAME: '");
        buffer.append(this.protocolName);
        buffer.append("' - RESERVED: '");
        buffer.append(this.reserved);
        buffer.append("' - INFO_HASH: '");
        buffer.append(ByteUtils.toHexString(this.infoHash));
        buffer.append("' - PEER_ID: '");
        buffer.append(this.peerId);

        return buffer.toString();
    }

    public String toByteString() {
        return ByteUtils.toHexString(this.getBytes());
    }
}