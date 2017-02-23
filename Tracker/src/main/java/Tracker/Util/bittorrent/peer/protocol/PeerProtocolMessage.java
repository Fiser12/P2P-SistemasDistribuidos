package Tracker.Util.bittorrent.peer.protocol;

import Tracker.Util.bittorrent.util.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public abstract class PeerProtocolMessage {

    public enum Type {
        KEEP_ALIVE(null),
        CHOKE(0),
        UNCHOKE(1),
        INTERESTED(2),
        NOT_INTERESTED(3),
        HAVE(4),
        BITFIELD(5),
        REQUEST(6),
        PIECE(7),
        CANCEL(8),
        PORT(9);

        private final Integer id;

        Type(Integer id) {
            this.id = id;
        }
        public Integer getId() {
            return this.id;
        }
    }

    private Type type;
    private byte[] length;
    private byte[] payload;

    public PeerProtocolMessage(Type type) {
        this.type = type;
    }

    public static PeerProtocolMessage parseMessage(byte[] msgBytes) {
        PeerProtocolMessage message = null;

        if (msgBytes != null && msgBytes.length != 0) {

            int length = ByteUtils.bigEndianBytesToInt(msgBytes, 0);

            if (length == 0) {
                return new KeepAliveMsg();
            }

            int id = msgBytes[4];

            switch (id) {
                case 0:    //choke
                    message = new ChokeMsg();
                    break;
                case 1:    //unchoke
                    message = new UnChokeMsg();
                    break;
                case 2:    //interested
                    message = new InterestedMsg();
                    break;
                case 3: //not_interested
                    message = new NotInterestedMsg();
                    break;
                case 4: //have
                    message = new HaveMsg(ByteUtils.bigEndianBytesToInt(msgBytes, 5));    //Piece index
                    break;
                case 5:    //bitfield
                    message = new BitfieldMsg(Arrays.copyOfRange(msgBytes, 5, msgBytes.length));    //Bitfield
                    break;
                case 6:    //request
                    message = new RequestMsg(ByteUtils.bigEndianBytesToInt(msgBytes, 5),    //Piece index
                            ByteUtils.bigEndianBytesToInt(msgBytes, 9),    //Block offset
                            ByteUtils.bigEndianBytesToInt(msgBytes, 13));    //Block length
                    break;
                case 7:    //piece
                    message = new PieceMsg(ByteUtils.bigEndianBytesToInt(msgBytes, 5),            //Piece index
                            ByteUtils.bigEndianBytesToInt(msgBytes, 9),            //Block offset
                            Arrays.copyOfRange(msgBytes, 13, msgBytes.length));    //Data
                    break;
                case 8:    //cancel
                    message = new CancelMsg(ByteUtils.bigEndianBytesToInt(msgBytes, 5),    //Piece index
                            ByteUtils.bigEndianBytesToInt(msgBytes, 9),    //Block offset
                            ByteUtils.bigEndianBytesToInt(msgBytes, 13));    //Block length
                    break;
                case 9:    //port
                    message = new PortMsg(ByteUtils.bigEndianBytesToInt(msgBytes, 5));    //Port number
                    break;
                default:
                    System.out.println("- Unknown message. Length: " + length + " - ID: " + id);
                    break;
            }
        }

        return message;
    }

    public Type getType() {
        return type;
    }

    public byte[] getLength() {
        return length;
    }

    public void setLength(byte[] length) {
        this.length = length;
    }

    public Integer getId() {
        return !type.equals(Type.KEEP_ALIVE) ? type.getId() : -1;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            if (length != null) {
                result.write(this.length);
            }

            if (this.type != null && !this.type.equals(Type.KEEP_ALIVE)) {
                result.write(this.getId());
            }

            if (payload != null) {
                result.write(this.payload);
            }

            return result.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("LENGTH: ");

        buffer.append(ByteUtils.byteArrayToInt(this.length));
        buffer.append(" - TYPE: ");
        buffer.append(this.type.toString());
        buffer.append("(id-");
        buffer.append(this.type.getId());
        buffer.append(")");

        return buffer.toString();
    }

    public String toByteString() {
        return ByteUtils.toHexString(this.getBytes());
    }
}