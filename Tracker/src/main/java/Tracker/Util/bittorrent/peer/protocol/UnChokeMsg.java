package Tracker.Util.bittorrent.peer.protocol;

import Tracker.Util.bittorrent.util.ByteUtils;

/**
 * unchoke: <len=0001><id=1>
 * <p>
 * The unchoke message is fixed-length and has no payload.
 */

public class UnChokeMsg extends PeerProtocolMessage {

    public UnChokeMsg() {
        super(Type.UNCHOKE);
        super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));
    }
}
