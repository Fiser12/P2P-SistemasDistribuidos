package Tracker.Util.bittorrent.peer.protocol;

import Tracker.Util.bittorrent.util.ByteUtils;

/**
 * interested: <len=0001><id=2>
 * <p>
 * The interested message is fixed-length and has no payload
 */

public class InterestedMsg extends PeerProtocolMessage {

    public InterestedMsg() {
        super(Type.INTERESTED);
        super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));
    }
}
