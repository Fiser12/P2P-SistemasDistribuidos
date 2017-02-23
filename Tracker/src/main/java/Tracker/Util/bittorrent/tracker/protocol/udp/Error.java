package Tracker.Util.bittorrent.tracker.protocol.udp;

import Tracker.Util.bittorrent.util.TorrentUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Offset  Size            	Name            	Value
 * 0       32-bit integer  	action          	3 // error
 * 4       32-bit integer  	transaction_id
 * 8       string  message
 */

public class Error extends BitTorrentUDPMessage {

    private String message;

    public Error() {
        super(Action.ERROR);
    }

    public static Error parse(byte[] byteArray) {
        //TODO Revisar
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        Error error = new Error();
        error.setAction(Action.valueOf(byteBuffer.getInt(0)));
        error.setTransactionId(byteBuffer.getInt(TorrentUtils.INT_SIZE));

        byte[] data = new byte[byteArray.length - 8];
        byteBuffer.position(8);
        byteBuffer.get(data);
        error.setMessage(new String(data));
        return error;
    }

    @Override
    public byte[] getBytes() {
        //TODO Revisar

        int size = 8 + message.getBytes().length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(0, getAction().value());
        byteBuffer.putInt(4, getTransactionId());
        byteBuffer.position(8);
        byteBuffer.put(message.getBytes());
        byteBuffer.flip();

        return byteBuffer.array();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
