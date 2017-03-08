package Peer.test;

import Tracker.Util.bittorrent.tracker.protocol.udp.*;
import Tracker.Util.bittorrent.tracker.protocol.udp.Error;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PeerTest {
    private int idFinal;
    private int port = 2000;
    private InetAddress trackerHost;
    private DatagramSocket datagramSocket;
    private ConnectResponse connectResponse;

    public PeerTest() throws UnknownHostException, SocketException {
        trackerHost = InetAddress.getByName("224.0.0.1");
        datagramSocket = new DatagramSocket();
    }
    public void start() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startSendConects();
                    serverListeing();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void startSendConects() throws SocketException {
        new Timer().schedule(this.updateId(), 0, 60 * 1000);
    }
    private void serverListeing() throws IOException {
        byte[] receiveData = new byte[1024];
        while(true){
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            datagramSocket.receive(packet);
            byte[] data = packet.getData();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer = buffer.order(ByteOrder.BIG_ENDIAN);
            processPetition(data, buffer);
        }
    }

    private void processPetition(byte[] receiveData, ByteBuffer buffer) {
        if (buffer != null) {
            int value = buffer.get(3);
            switch(value){
                case 0:
                    connectResponse = ConnectResponse.parse(receiveData);
                    if(connectResponse.getBytes().length >= 16){
                        idFinal = connectResponse.getTransactionId();
                        System.err.println("ID RECIBIDO: " + idFinal);
                        try {
                            announceRequestTest(datagramSocket, trackerHost, AnnounceRequest.Event.NONE);
                        } catch (IOException ignored) {
                        }
                    }else{
                        System.err.println("PEER: ERROR CONNECTION");
                    }
                    break;
                case 1:
                    AnnounceResponse announceResponse = AnnounceResponse.parse(receiveData);
                    if(announceResponse.getBytes().length >= 16) {
                        System.out.println("PEER: ANNOUNCE OK LECHEERS: " + announceResponse.getLeechers() + "SEEDERS: " + announceResponse.getSeeders() + "NUMBER OF PEERS: " + announceResponse.getSeeders()+announceResponse.getLeechers() + "   " + announceResponse.getPeers().size());
                        //try {
                            //scrapeRequestTest(datagramSocket, trackerHost);
                        //} catch (IOException ignored) {
                        //}
                        //TODO: NO VA EL SCRAPE BIEN POR LA CONVERSION DEL HEX
                    }else{
                        System.err.println("PEER: ERROR ANNOUNCE");
                    }
                    break;
                case 2:
                    ScrapeResponse scrapeResponse = ScrapeResponse.parse(receiveData);
                    if(scrapeResponse.getBytes().length >= 16) {
                        System.out.println("PEER: SCRAPE OK");
                    }else{
                        System.err.println("PEER: ERROR SCRAPE");
                    }
                    break;
                case 3:
                    Error e = Error.parse(receiveData);
                    System.err.println("Error recibido: " + e.getMessage());
                    break;
            }
        }
    }
    private int idSend;
    private void connectionRequest(DatagramSocket datagramSocket, InetAddress trackerHost) throws IOException {
        ConnectRequest connectRequest = new ConnectRequest();
        Random random = new Random();
        idSend = random.nextInt(Integer.MAX_VALUE);
        connectRequest.setTransactionId(idSend);
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
                    connectionRequest(datagramSocket, trackerHost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
    private void announceRequestTest(DatagramSocket datagramSocket, InetAddress trackerHost, AnnounceRequest.Event requestedEvent) throws IOException {
        AnnounceRequest request = new AnnounceRequest();
        request.setConnectionId(connectResponse.getConnectionId());
        request.setAction(BitTorrentUDPMessage.Action.ANNOUNCE);
        request.setTransactionId(connectResponse.getTransactionId());
        String hex = "014245AE00000000000000000000000000000000";
        request.setInfoHash(new BigInteger(hex,16).toByteArray());
        request.setPeerId(String.valueOf(this.idFinal));
        request.setDownloaded(2000);
        request.setLeft(200);
        request.setUploaded(3000);
        request.setEvent(requestedEvent);
        PeerInfo info = new PeerInfo();
        info.setIpAddress(0);
        info.setPort(this.port);
        request.setPeerInfo(info);
        request.setKey(new Random().nextInt(Integer.MAX_VALUE));
        request.setNumWant(-1);
        byte[] requestBytes = request.getBytes();
        DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, trackerHost, port);
        System.out.println("Announce mandado a " + trackerHost.getHostAddress());
        datagramSocket.send(packet);
    }
    private void scrapeRequestTest(DatagramSocket datagramSocket, InetAddress trackerHost) throws IOException {
        ScrapeRequest request = new ScrapeRequest();
        request.setConnectionId(connectResponse.getConnectionId() );
        request.setTransactionId(connectResponse.getTransactionId());
        request.addInfoHash("014245AE00000000000000000000000000000000");
        byte[] requestBytes = request.getBytes();
        DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, trackerHost, port);
        System.out.println("Announce mandado a " + trackerHost.getHostAddress());
        datagramSocket.send(packet);
    }
}
