package Tracker.Util.bittorrent.tracker.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Offset  Size            	Name            	Value
 * 0       32-bit integer  	action          	0 // connect
 * 4       32-bit integer  	transaction_id
 * 8       64-bit integer  	connection_id
 * 16
 */
public class ConnectResponse extends BitTorrentUDPRequestMessage {

    public ConnectResponse() {
        super(Action.CONNECT);
    }

    public static ConnectResponse parse(byte[] byteArray) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(byteArray);
            buffer.order(ByteOrder.BIG_ENDIAN);

            ConnectResponse msg = new ConnectResponse();

            msg.setAction(Action.valueOf(buffer.getInt(0)));
            msg.setTransactionId(buffer.getInt(4));
            msg.setConnectionId(buffer.getLong(8));

            return msg;
        } catch (Exception ex) {
            System.out.println("# Error parsing ConnectResponse message: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putInt(0, super.getAction().value());
        buffer.putInt(4, super.getTransactionId());
        buffer.putLong(8, super.getConnectionId());

        buffer.flip();

        return buffer.array();
    }
}