package Tracker.Util.bittorrent.tracker.protocol.udp.test;

import Tracker.Util.bittorrent.tracker.protocol.udp.ConnectRequest;
import Tracker.Util.bittorrent.tracker.protocol.udp.ConnectResponse;
import Tracker.Util.bittorrent.util.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class PeerTrackerTest {

    public static final String TRACKER_NAME = "tracker.opentrackr.org";
    public static final int TRACKER_PORT = 1337;
    public static final String INFO_HASH = "3c904e69ab92029d13bf8d812972e882335514d3";

    public static void main(String[] args) {
        try{
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(15000);
            InetAddress serverHost = InetAddress.getByName(TRACKER_NAME);

            Random random = new Random();
            int transactionID = random.nextInt(Integer.MAX_VALUE);

            ConnectRequest request = new ConnectRequest();
            request.setTransactionId(transactionID);
            byte[] requestBytes = request.getBytes();
            DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, serverHost, TRACKER_PORT);
            udpSocket.send(packet);

            StringBuffer bufferOut = new StringBuffer("Connect Request\n - Action: ");
            bufferOut.append(request.getAction());
            bufferOut.append("\n - TransactionID: ");
            bufferOut.append(request.getTransactionId());
            bufferOut.append("\n - ConnectionID: ");
            bufferOut.append(request.getConnectionId());
            bufferOut.append("\n - Bytes: ");
            bufferOut.append(ByteUtils.toHexString(requestBytes));

            byte[] responseBytes = new byte[16]; //16 bytes is the size of Connect Response Message
            packet = new DatagramPacket(responseBytes, responseBytes.length);
            udpSocket.receive(packet);

            if (packet.getLength() >= 16) {
                ConnectResponse response = ConnectResponse.parse(packet.getData());
                bufferOut.append("\n\nConnect Response\n - Action: ");
                bufferOut.append(response.getAction());
                bufferOut.append("\n - TransactionID: ");
                bufferOut.append(response.getTransactionId());
                bufferOut.append("\n - ConnectionID: ");
                bufferOut.append(response.getConnectionId());
                bufferOut.append("\n - Bytes: ");
                bufferOut.append(ByteUtils.toHexString(responseBytes));
            } else {
                bufferOut.append("- ERROR: Response length to small ");
                bufferOut.append(packet.getLength());
            }

            System.out.println(bufferOut.toString());
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
}