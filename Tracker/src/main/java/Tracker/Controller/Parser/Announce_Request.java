package Tracker.Controller.Parser;

import Tracker.Util.bittorrent.tracker.protocol.udp.*;

import java.net.InetAddress;

public class Announce_Request implements UDP_Message {
    private static Announce_Request instance;

    public static Announce_Request getInstance() {
        if(instance == null)
            instance = new Announce_Request();
        return instance;
    }

    public BitTorrentUDPRequestMessage parse(byte[] data) {
        return AnnounceRequest.parse(data);
    }
    public boolean validate(BitTorrentUDPRequestMessage request, InetAddress clientAddress) {
        AnnounceRequest announceRequest = (AnnounceRequest) request;
        return announceRequest.getBytes().length == 98 && announceRequest.getAction() == BitTorrentUDPMessage.Action.ANNOUNCE ;
               // && trackerUDPServer.isConnectionIdStillValid(announceRequest.getConnectionId());
        //TODO
    }
    public byte[] getResponse(BitTorrentUDPRequestMessage request, InetAddress clientAddress, int clientPort) {
        AnnounceRequest requestMesage = (AnnounceRequest) request;
        AnnounceResponse announceResponse = new AnnounceResponse();
        announceResponse.setTransactionId(request.getTransactionId());
        // TODO add peer info of that given file. search by infohash string
        /*
        SwarmInfo info = tracker.findAnnounceInfoOf(requestMesage.getHexInfoHash());
        if (info != null) {
            announceResponse.setInterval(info.getInterval());
            announceResponse.setLeechers(info.getLeechers());
            announceResponse.setSeeders(info.getSeeders());
            announceResponse.setPeers(info.getPeers());
            return announceResponse.getBytes();
        }
        */
        return getError(request);

    }
    public byte[] getError(BitTorrentUDPRequestMessage request) {
        //TODO Hacer el getERROR
        return null;
    }
}
