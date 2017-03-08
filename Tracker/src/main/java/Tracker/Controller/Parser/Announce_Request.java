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
        return announceRequest.getBytes().length == 98 &&
                announceRequest.getAction() == BitTorrentUDPMessage.Action.ANNOUNCE;
    }
    public synchronized byte[] sendResponse(BitTorrentUDPRequestMessage request, InetAddress clientAddress, int clientPort) {
        AnnounceRequest requestMesage = (AnnounceRequest) request;
        Smarms swarm = SQLiteUtil.getInstance().getData(requestMesage.getHexInfoHash(), Smarms.class);
        Peer peer = SQLiteUtil.getInstance().getData(String.valueOf(requestMesage.getConnectionId()), Peer.class);
        PeerSmarms peerSmarms = new PeerSmarms();
        peerSmarms.setSmarms(swarm);
        peerSmarms.setPeer(peer);
        peerSmarms.setBytesDescargados(requestMesage.getDownloaded());
        try {
            SQLiteUtil.getInstance().saveData(peerSmarms, this);
        }catch(NullPointerException e){
            return sendError(request, "Error en el proceso de announce, la sesión ha caducado");
        }
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<PeerSmarms> peerSmarmList = SQLiteUtil.getInstance().listPeerSmarms();
        if (swarm != null) {
            AnnounceResponse announceResponse = sacarSeedersYLeechers(swarm, peerSmarmList);
            announceResponse.setTransactionId(request.getTransactionId());
            return announceResponse.getBytes();
        }
        return sendError(request, "Error en el proceso de announce");

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
                    //TODO No va el envio de los peerInfo bien, el resto si
                    peerInfo.setIpAddress(Integer.parseInt(temp.getPeer().getIp().replace(".","")));
                    peerInfo.setPort(temp.getPeer().getPort());
                    peerInfoList.add(peerInfo);
                }
        AnnounceResponse announceResponse = new AnnounceResponse();
        announceResponse.setInterval(60);
        announceResponse.setLeechers(leecher.size());
        announceResponse.setSeeders(seeder.size());
        announceResponse.setPeers(peerInfoList);
        return announceResponse;
    }
    public byte[] sendError(BitTorrentUDPRequestMessage request, String errorString) {
        if(TrackerService.getInstance().getTracker().isMaster()) {
            Error error = new Error();
            error.setMessage(errorString);
            error.setTransactionId(request.getTransactionId());
            return error.getBytes();
        }
        return null;
    }
}
