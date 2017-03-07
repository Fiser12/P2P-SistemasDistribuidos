package Tracker.Controller;

import Tracker.Controller.Parser.Announce_Request;
import Tracker.Controller.Parser.Connection_Request;
import Tracker.Controller.Parser.Scrape_Request;
import Tracker.Controller.Parser.UDP_Message;
import Tracker.Util.SQLiteUtil;
import Tracker.Util.bittorrent.tracker.protocol.udp.BitTorrentUDPRequestMessage;

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
    private InetAddress inetAddress;
    private static final long TIEMPO_SESION = 60 * 1000;

    private UDPManager() {
        this.udpServerAlive = true;
    }
    public void start(){
        new Thread() {
            public void run() {
                try {
                    hiloEliminarSesiones ();
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
        inetAddress = InetAddress.getByName(TrackerService.getInstance().getTracker().getIp());
        multicastSocket = new MulticastSocket(TrackerService.getInstance().getTracker().getPortPeers());
        multicastSocket.joinGroup(inetAddress);
        byte[] receiveData = new byte[1024];

        while (udpServerAlive) {
            DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
            multicastSocket.receive(datagramPacket);
            procesarPeticion(datagramPacket.getData(), datagramPacket.getAddress(), datagramPacket.getPort(), multicastSocket);
        }
        multicastSocket.leaveGroup(inetAddress);
        multicastSocket.close();
    }
    private void hiloEliminarSesiones () {

        Thread hiloEliminarSesiones = new Thread() {

            public void run() {
                try {
                    while(udpServerAlive) {
                        Thread.sleep(TIEMPO_SESION);
                        SQLiteUtil.getInstance().eliminarSesiones();
                    }
                } catch (InterruptedException e1) {
                    System.err.println("# Interrupted Exception: " + e1.getMessage());
                }

            }
        };

        hiloEliminarSesiones.start();
    }
    private void procesarPeticion(byte[] data, InetAddress clientAddress, int clientPort, DatagramSocket datagramSocket) throws IOException {
        if (TrackerService.getInstance().getTracker().isMaster()) {
            byte[] response = null;
            try {
                ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                if (byteBuffer != null) {
                    int value = byteBuffer.getInt(8);
                    UDP_Message parser = prepararParser(value);
                    if(parser!=null){
                        BitTorrentUDPRequestMessage parsed = parser.parse(data);
                        if(parsed!=null){
                            boolean ok = parser.validate(parsed, clientAddress);
                            if (ok) {
                                response = parser.sendResponse(parsed, clientAddress, clientPort);
                            } else {
                                response = parser.sendError(parsed, "Error validation");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (response != null && response.length > 0) {
                DatagramPacket sendPacket = new DatagramPacket(response, response.length, clientAddress, clientPort);
                datagramSocket.send(sendPacket);
            }
        }
    }
    private UDP_Message prepararParser(int value){
        UDP_Message parser = null;
        switch(value){
            case 0:
                parser = Connection_Request.getInstance();
                break;
            case 1:
                parser = Announce_Request.getInstance();
                break;
            case 2:
                parser = Scrape_Request.getInstance();
                break;
        }
        return parser;
    }
}
