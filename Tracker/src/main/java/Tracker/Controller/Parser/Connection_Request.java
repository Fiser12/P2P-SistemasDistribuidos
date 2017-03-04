package Tracker.Controller.Parser;

import Tracker.Controller.TrackerService;
import Tracker.Model.Peer;
import Tracker.Util.SQLiteUtil;
import Tracker.Util.bittorrent.tracker.protocol.udp.BitTorrentUDPRequestMessage;
import Tracker.Util.bittorrent.tracker.protocol.udp.ConnectRequest;
import Tracker.Util.bittorrent.tracker.protocol.udp.ConnectResponse;
import Tracker.Util.bittorrent.tracker.protocol.udp.Error;

import java.net.InetAddress;
import java.util.Random;

public class Connection_Request implements UDP_Message
{
    private static Connection_Request instance;
    public static Connection_Request getInstance() {
        if(instance == null)
            instance = new Connection_Request();
        return instance;
    }
    public BitTorrentUDPRequestMessage parse(byte[] data)
    {
        return ConnectRequest.parse(data);
    }
    public boolean validate(BitTorrentUDPRequestMessage request, InetAddress clientAddress)
    {
        return request.getConnectionId() == Long.decode("0x41727101980");
    }
    public byte[] getResponse(BitTorrentUDPRequestMessage request, InetAddress clientAddress, int clientPort)
    {
        ConnectResponse response = new ConnectResponse();
        response.setTransactionId(request.getTransactionId());
        Random random = new Random();
        long randomInt = random.nextLong();
        response.setConnectionId(randomInt);
        Peer peer = new Peer();
        peer.setIdPeer(Long.valueOf(request.getTransactionId()));
        peer.setIp(clientAddress.getHostAddress());
        peer.setConnectionId(new Long(randomInt));
        peer.setPort(clientPort);
        SQLiteUtil.saveData(peer);
        return response.getBytes();
    }
    public byte[] getError(BitTorrentUDPRequestMessage request)
    {
        if(TrackerService.getInstance().getTracker().isMaster()) {
            Error error = new Error();
            error.setMessage("Error en el proceso de conexión");
            error.setTransactionId(request.getTransactionId());
            return error.getBytes();
        }
        return null;
    }
}
