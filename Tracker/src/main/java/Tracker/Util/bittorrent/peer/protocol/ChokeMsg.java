package Tracker.Util.bittorrent.peer.protocol;

import Tracker.Util.bittorrent.util.ByteUtils;

/**
 * choke: <len=0001><id=0>
 * <p>
 * The choke message is fixed-length and has no payload.
 */

public class ChokeMsg extends PeerProtocolMessage {

    public ChokeMsg() {
        super(Type.CHOKE);
        super.setLength(ByteUtils.intToBigEndianBytes(1, new byte[4], 0));
    }
}