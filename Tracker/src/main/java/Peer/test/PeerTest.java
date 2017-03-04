package Peer.test;

import Tracker.Util.bittorrent.tracker.protocol.udp.ConnectRequest;

import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PeerTest {
    private int id;
    private int port = 2000;
    private InetAddress serverHost;
    private DatagramSocket clientSocket;

    public PeerTest() throws UnknownHostException, SocketException {
        serverHost = InetAddress.getByName("224.0.0.1");
        clientSocket = new DatagramSocket();
    }
    public void start() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startSendConects();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void startSendConects() throws SocketException {
        new Timer().schedule(this.updateId(), 0, 60 * 1000);
    }

    private void connectionRequest(DatagramSocket datagramSocket, InetAddress trackerHost) throws IOException {
        ConnectRequest connectRequest = new ConnectRequest();
        Random random = new Random();
        id = random.nextInt(Integer.MAX_VALUE);
        connectRequest.setTransactionId(id);
        byte[] requestBytes = connectRequest.getBytes();
        DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, trackerHost, port);
        System.out.println("Connection mandado a " + trackerHost.getHostAddress());
        datagramSocket.send(packet);
    }
    private TimerTask updateId() {
        return new TimerTask() {
            @Override
            public void run() {
            try {
                    connectionRequest(clientSocket, serverHost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
