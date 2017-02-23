package Tracker.Util.bittorrent.util.test;

import Tracker.Util.bittorrent.util.ByteUtils;

public class ByteUtilTest {
    public static void main(String[] args) {
        long number = 1l;

        System.out.println("- Original number: " + number);

        byte[] bufferBig = ByteUtils.toBigEndian(number);

        System.out.print("- Bytes in Big Endian: ");

        for (byte b : bufferBig) {
            System.out.print("'" + b + "' ");
        }

        System.out.println();

        byte[] bufferLittle = ByteUtils.toLittleEndian(number);

        System.out.print("- Bytes in Little Endian: ");

        for (byte b : bufferLittle) {
            System.out.print("'" + b + "' ");
        }

        System.out.println();
        System.out.println("- Bytes in Big Endian to long: " + ByteUtils.bigEndianToLong(bufferBig));
        System.out.println("- Bytes in Little Endian to long: " + ByteUtils.littleEndianToLong(bufferLittle));
        System.out.println();

        //UPDATE: 23/12/2015 - Test for the transformation between hex string and byte array
        System.out.println("Hex infoHash: 916A6189FFB20F0B20739E6F760C99174625DC2B");
        byte[] byteArray = ByteUtils.toByteArray("916A6189FFB20F0B20739E6F760C99174625DC2B");
        System.out.println("Byte array size: " + byteArray.length);
        System.out.println("New infoHash: " + ByteUtils.toHexString(byteArray));
    }
}
