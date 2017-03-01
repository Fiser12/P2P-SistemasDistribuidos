package Tracker.Controller.Parser;

import Tracker.Controller.TrackerService;
import Tracker.Model.Peer;
import Tracker.Model.PeerSmarms;
import Tracker.Model.Smarms;
import Tracker.Util.SQLiteUtil;
import Tracker.Util.bittorrent.tracker.protocol.udp.*;
import Tracker.Util.bittorrent.tracker.protocol.udp.Error;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

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
        List<Peer> peerList = SQLiteUtil.listPeer();
        boolean exists = false;
        for(Peer peer: peerList){
            if(peer.getConnectionId()==announceRequest.getConnectionId()){
                exists = true;
            }
        }
        return announceRequest.getBytes().length == 98 &&
                announceRequest.getAction() == BitTorrentUDPMessage.Action.ANNOUNCE &&
                exists;
    }
    public byte[] getResponse(BitTorrentUDPRequestMessage request, InetAddress clientAddress, int clientPort) {
        AnnounceRequest requestMesage = (AnnounceRequest) request;

        Smarms swarm = (Smarms) SQLiteUtil.getData(requestMesage.getHexInfoHash(), Smarms.class);
        PeerSmarms peerSmarms = new PeerSmarms();
        peerSmarms.setSmarms(swarm);
        peerSmarms.setPeer((Peer) SQLiteUtil.getData(String.valueOf(requestMesage.getConnectionId()), Peer.class));
        peerSmarms.setBytesDescargados(requestMesage.getDownloaded());
        SQLiteUtil.saveData(peerSmarms);
        List<PeerSmarms> peerSmarmList = SQLiteUtil.listPeerSmarms();

        if (swarm != null) {
            AnnounceResponse announceResponse = sacarSeedersYLeechers(swarm, peerSmarmList);
            announceResponse.setTransactionId(request.getTransactionId());
            return announceResponse.getBytes();
        }
        return getError(request);

    }
    private AnnounceResponse sacarSeedersYLeechers(Smarms smarms, List<PeerSmarms> peerSmarmList)
    {
        ArrayList<PeerSmarms> seeder = new ArrayList<>();
        ArrayList<PeerSmarms> leecher = new ArrayList<>();
        ArrayList<PeerInfo> peerInfoList = new ArrayList<>();

        if(smarms!=null)
            for(PeerSmarms temp: peerSmarmList)
                if(temp.getSmarms().getHexInfoHash().equals(smarms.getHexInfoHash())) {
                    if (temp.getBytesDescargados().equals(smarms.getTamanoEnBytes()))
                        seeder.add(temp);
                    else
                        leecher.add(temp);
                    PeerInfo peerInfo = new PeerInfo();
                    peerInfo.setIpAddress(Integer.parseInt(temp.getPeer().getIp().replace(".", "")));
                    peerInfo.setPort(temp.getPeer().getPort());
                    peerInfoList.add(peerInfo);
                }
        AnnounceResponse announceResponse = new AnnounceResponse();
        announceResponse.setInterval(30);
        announceResponse.setLeechers(leecher.size());
        announceResponse.setSeeders(seeder.size());
        announceResponse.setPeers(peerInfoList);
        return announceResponse;
    }

    public byte[] getError(BitTorrentUDPRequestMessage request) {
        if(TrackerService.getInstance().getTracker().isMaster()) {
            Error error = new Error();
            error.setMessage("Error en el proceso de announce");
            error.setTransactionId(request.getTransactionId());
            return error.getBytes();
        }
        return null;
    }
}
