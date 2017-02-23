package Tracker.Util.bittorrent.peer.protocol.test;

import Tracker.Util.bittorrent.peer.protocol.*;
import Tracker.Util.bittorrent.util.ByteUtils;

import java.util.BitSet;

public class PeerMessagesTest {

    public static void main(String args[]) {
        Handsake handsake = new Handsake();

        String peerId = ByteUtils.generatePeerId();
        byte[] infoHash = ByteUtils.toByteArray("916A6189FFB20F0B20739E6F760C99174625DC2B");

        handsake.setPeerId(peerId);
        handsake.setInfoHash(infoHash);
        printMessage(handsake);

        PeerProtocolMessage msg;

        msg = new KeepAliveMsg();
        printMessage(msg);

        msg = new ChokeMsg();
        printMessage(msg);

        msg = new UnChokeMsg();
        printMessage(msg);

        msg = new InterestedMsg();
        printMessage(msg);

        msg = new NotInterestedMsg();
        printMessage(msg);

        int index = 1;
        int begin = 0;
        int length = 128;
        int port = 35500;

        msg = new HaveMsg(index);
        printMessage(msg);

        //Create and empty BitSet of 16 possitions
        BitSet bitSet = new BitSet(16);
        //Sets the first and last possitions of the bitSet
        bitSet.set(0);
        bitSet.set(15);
        //Converts the BitSet to a byte[]
        byte[] bitfield = new byte[16];
        ByteUtils.bitSetToBytes(bitSet, bitfield);

        msg = new BitfieldMsg(bitfield);
        printMessage(msg);

        msg = new RequestMsg(index, begin, length);
        printMessage(msg);

        msg = new PieceMsg(index, begin, new byte[32]);
        printMessage(msg);

        msg = new CancelMsg(index, begin, length);
        printMessage(msg);

        msg = new PortMsg(port);
        printMessage(msg);
    }

    public static void printMessage(PeerProtocolMessage msg) {
        StringBuffer buffer = new StringBuffer("\n");
        buffer.append(msg.getType());
        buffer.append("\n");
        buffer.append(msg);
        buffer.append("\nHexBytes: ");
        buffer.append(msg.toByteString());
        buffer.append(" (");
        buffer.append(msg.toByteString().length() / 2);
        buffer.append(" bytes with length field)");

        System.out.println(buffer.toString());
    }

    public static void printMessage(Handsake msg) {
        StringBuffer buffer = new StringBuffer("HANDSAKE\n");
        buffer.append(msg);
        buffer.append("\nHexBytes: ");
        buffer.append(msg.toByteString());
        buffer.append(" (");
        buffer.append(msg.toByteString().length() / 2);
        buffer.append(" bytes with length field)");

        System.out.println(buffer.toString());
    }
}