package Tracker.Controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPManager {

    private MulticastSocket multicastSocket;
    private boolean udpServerAlive;
    private static UDPManager instance = null;

    private UDPManager() {
        this.udpServerAlive = true;
    }
    public void start(){
        new Thread() {
            public void run() {
                try {
                    launchServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public static UDPManager getInstance(){
        if (instance == null) {
            instance = new UDPManager();
        }
        return instance;
    }


    private void launchServer() throws IOException {
        InetAddress inetAddress;
        inetAddress = InetAddress.getByName(TrackerService.getInstance().getTracker().getIp());
        multicastSocket = new MulticastSocket(TrackerService.getInstance().getTracker().getPortPeers());
        multicastSocket.joinGroup(inetAddress);
        byte[] receiveData = new byte[1024];

        while (udpServerAlive) {
            DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
            multicastSocket.receive(datagramPacket);
            processData(datagramPacket.getData(), datagramPacket.getAddress(), datagramPacket.getPort(), multicastSocket);
        }
        multicastSocket.leaveGroup(inetAddress);
        multicastSocket.close();
    }

    private void processData(byte[] data, InetAddress ipClient, int clientPort, DatagramSocket datagramSocket) throws IOException {
        if (TrackerService.getInstance().getTracker().isMaster()) {
            byte[] response = parseData(ipClient, clientPort, data);
            if (response != null && response.length > 0) {
                DatagramPacket sendPacket = new DatagramPacket(response, response.length, ipClient, clientPort);
                datagramSocket.send(sendPacket);
            }
        }
    }

    private byte[] parseData(InetAddress clientAddress, int clientPort, byte[] receivedBytes) {
        try {
            ByteBuffer buffer = this.deserialize(receivedBytes);
            /*
            if (buffer != null) {

                 int value = buffer.getInt(8);
                BitTorrentUDPRequestMessage parsedRequestMessage = PeerRequestParser.parse(value, receivedBytes);
                if (parsedRequestMessage != null) {
                    //validate received message
                    boolean valid = PeerRequestParser.validate(this, value, parsedRequestMessage);
                    if (valid) {
                        PeerRequestParser.triggerOnReceiveEvent(this, value, clientAddress, clientPort, parsedRequestMessage);
                        //valid message. response
                        return PeerRequestParser.getResponse(this, value, parsedRequestMessage);
                    } else {
                        this.trackerInstance.addLogLine("Error: Invalid message detected of type " + parsedRequestMessage.getClass().getSimpleName());
                        return PeerRequestParser.getError(value, false, parsedRequestMessage, "invalid message");
                    }
                } else {
                    this.trackerInstance.addLogLine("Error: NULL message detected ");
                    return PeerRequestParser.getError(value, false, null, "Malformed message");
                }

            }
            */
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
        return null;
    }

    private ByteBuffer deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer;
    }
}
