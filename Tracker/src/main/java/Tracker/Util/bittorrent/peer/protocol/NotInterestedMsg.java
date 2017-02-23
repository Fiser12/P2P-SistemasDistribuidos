package Tracker.Util.bittorrent.peer.protocol;

import Tracker.Util.bittorrent.util.ByteUtils;

/**
 * not interested: <len=0001><id=3>
 * <p>
 * The not interested message is fixed-length and has no payload.
 */

public class NotInterestedMsg extends PeerProtocolMessage {

    public NotInterestedMsg() {
        super(Type.NOT_INTERESTED);
        super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));
    }
}
