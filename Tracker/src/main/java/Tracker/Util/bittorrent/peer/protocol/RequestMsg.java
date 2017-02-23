package Tracker.Util.bittorrent.peer.protocol;

/**
 * request: <len=0013><id=6><index><begin><length>
 * <p>
 * The request message is fixed length, and is used to request a block.
 * The payload contains the following information:
 * - index: integer specifying the zero-based piece index.
 * - begin: integer specifying the zero-based byte offset within the piece.
 * - length: integer specifying the requested length.
 */

import Tracker.Util.bittorrent.util.ByteUtils;

import java.io.ByteArrayOutputStream;

public class RequestMsg extends PeerProtocolMessage {

    private int index;
    private int begin;
    private int rlength;

    public RequestMsg(int index, int begin, int rlength) {
        super(Type.REQUEST);
        super.setLength(ByteUtils.intToBigEndianBytes(13, new byte[4], 0));
        this.updatePayload(index, begin, rlength);

        this.index = index;
        this.begin = begin;
        this.rlength = rlength;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getRLength() {
        return rlength;
    }

    public void setRLength(int rlength) {
        this.rlength = rlength;
    }

    private void updatePayload(int index, int begin, int rlength) {
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();

            payload.write(ByteUtils.intToBigEndianBytes(index, new byte[4], 0));
            payload.write(ByteUtils.intToBigEndianBytes(begin, new byte[4], 0));
            payload.write(ByteUtils.intToBigEndianBytes(rlength, new byte[4], 0));

            super.setPayload(payload.toByteArray());
        } catch (Exception ex) {
            System.out.println("# Error updating RequestMsg payload: " + ex.getMessage());
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(super.toString());
        buffer.append(" - INDEX: ");
        buffer.append(this.index);
        buffer.append(" - BEGIN: ");
        buffer.append(this.begin);
        buffer.append(" - LENGTH: ");
        buffer.append(this.rlength);

        return buffer.toString();
    }
}