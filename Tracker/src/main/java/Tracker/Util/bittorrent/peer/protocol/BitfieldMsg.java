package Tracker.Util.bittorrent.peer.protocol;

import Tracker.Util.bittorrent.util.ByteUtils;

import java.io.ByteArrayOutputStream;

/**
 * bitfield: <len=0001+X><id=5><bitfield>
 * <p>
 * The bitfield message may only be sent immediately after the handshaking sequence
 * is completed, and before any other messages are sent. It is optional, and need not
 * be sent if a client has no pieces.
 * <p>
 * The bitfield message is variable length, where X is the length of the bitfield.
 * The payload is a bitfield representing the pieces that have been successfully
 * downloaded. The high bit in the first byte corresponds to piece index 0.
 * Bits that are cleared indicated a missing piece, and set bits indicate
 * a valid and available piece. Spare bits at the end are set to zero.
 * <p>
 * A bitfield of the wrong length is considered an error. Clients should drop
 * the connection if they receive bitfields that are not of the correct size,
 * or if the bitfield has any of the spare bits set.
 */
public class BitfieldMsg extends PeerProtocolMessage {

    private byte[] bitfield;

    public BitfieldMsg(byte[] bitfield) {
        super(Type.BITFIELD);
        super.setLength(ByteUtils.intToBigEndianBytes(1 + bitfield.length, new byte[4], 0));
        this.updatePayload(bitfield);

        this.bitfield = bitfield;
    }

    private void updatePayload(byte[] bitfield) {
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();

            payload.write(bitfield);

            super.setPayload(payload.toByteArray());
        } catch (Exception ex) {
            System.out.println("# Error updating BitfieldMsg payload: " + ex.getMessage());
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(super.toString());

        buffer.append(" - BITFIELD (");
        buffer.append(bitfield.length);
        buffer.append(" bits): ");
        buffer.append(ByteUtils.toHexString(bitfield));

        return buffer.toString();
    }
}