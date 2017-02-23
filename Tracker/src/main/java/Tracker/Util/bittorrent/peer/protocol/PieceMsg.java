package Tracker.Util.bittorrent.peer.protocol;

/**
 * piece: <len=0009+X><id=7><index><begin><block>
 * <p>
 * The piece message is variable length, where X is the length of the block.
 * The payload contains the following information:
 * - index: integer specifying the zero-based piece index
 * - begin: integer specifying the zero-based byte offset within the piece
 * - block: block of data, which is a subset of the piece specified by index.
 */

import Tracker.Util.bittorrent.util.ByteUtils;

import java.io.ByteArrayOutputStream;

public class PieceMsg extends PeerProtocolMessage {

    private int index;
    private int begin;
    private byte[] block;

    public PieceMsg(int index, int begin, byte[] block) {
        super(Type.PIECE);
        super.setLength(ByteUtils.intToBigEndianBytes(9 + block.length, new byte[4], 0));
        this.updatePayload(index, begin, block);

        this.index = index;
        this.begin = begin;
        this.block = block;
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

    public byte[] getBlock() {
        return block;
    }

    public void setBlock(byte[] block) {
        this.block = block;
    }

    private void updatePayload(int index, int begin, byte[] block) {
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();

            payload.write(ByteUtils.intToBigEndianBytes(index, new byte[4], 0));
            payload.write(ByteUtils.intToBigEndianBytes(begin, new byte[4], 0));
            payload.write(block);

            super.setPayload(payload.toByteArray());
        } catch (Exception ex) {
            System.out.println("# Error updating PieceMsg payload: " + ex.getMessage());
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(super.toString());

        buffer.append(" - INDEX: ");
        buffer.append(this.index);
        buffer.append(" - BEGIN: ");
        buffer.append(this.begin);
        buffer.append(" - ");
        buffer.append(this.block.length);
        buffer.append(" BYTES OF DATA");

        return buffer.toString();
    }
}