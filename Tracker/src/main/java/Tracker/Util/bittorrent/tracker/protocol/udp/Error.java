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
        ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
        Error error = new Error();
        error.setAction(Action.valueOf(bufferReceive.getInt(0)));
        error.setTransactionId(bufferReceive.getInt(TorrentUtils.INT_SIZE));
        byte[] messageData = new byte[byteArray.length - 8];
        bufferReceive.position(8);
        bufferReceive.get(messageData);
        error.setMessage(new String(messageData));
        return error;
    }

    @Override
    public byte[] getBytes() {
        //TODO Revisar

        int initialSize = 8;
        int size = initialSize + message.getBytes().length;
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
