package Tracker.Util.bittorrent.peer.protocol;

import Tracker.Util.bittorrent.util.ByteUtils;

import java.io.ByteArrayOutputStream;

/**
 * have: <len=0005><id=4><piece index>
 * <p>
 * The have message is fixed length. The payload is the zero-based index of a piece that has just been successfully
 * downloaded and verified via the hash.
 * <p>
 * Implementer's Note: That is the strict definition, in reality some games may be played. In particular because
 * peers are extremely unlikely to download pieces that they already have, a peer may choose not to advertise
 * having a piece to a peer that already has that piece. At a minimum "HAVE suppression" will result in a 50%
 * reduction in the number of HAVE messages, this translates to around a 25-35% reduction in protocol overhead.
 */

public class HaveMsg extends PeerProtocolMessage {

    private int index;

    public HaveMsg(int index) {
        super(Type.HAVE);
        super.setLength(ByteUtils.intToBigEndianBytes(5, new byte[4], 0));
        this.updatePayload(index);

        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private void updatePayload(int index) {
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();

            payload.write(ByteUtils.intToBigEndianBytes(index, new byte[4], 0));

            super.setPayload(payload.toByteArray());
        } catch (Exception ex) {
            System.out.println("# Error updating HaveMsg payload: " + ex.getMessage());
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(super.toString());
        buffer.append(" - INDEX: ");
        buffer.append(this.index);

        return buffer.toString();
    }
}