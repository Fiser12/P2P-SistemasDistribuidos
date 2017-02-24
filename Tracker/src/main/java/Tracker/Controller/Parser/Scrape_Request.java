package Tracker.Controller.Parser;

import Tracker.Controller.TrackerService;
import Tracker.Model.Peer;
import Tracker.Model.PeerSmarms;
import Tracker.Model.PeerSmarmsId;
import Tracker.Model.Smarms;
import Tracker.Util.HibernateUtil;
import Tracker.Util.bittorrent.tracker.protocol.udp.*;
import Tracker.Util.bittorrent.tracker.protocol.udp.Error;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Scrape_Request implements UDP_Message {
    private static Scrape_Request instance;
    public static Scrape_Request getInstance() {
        if(instance == null)
            instance = new Scrape_Request();
        return instance;
    }

    public BitTorrentUDPRequestMessage parse(byte[] data) {
        return ScrapeRequest.parse(data);
    }
    public boolean validate(BitTorrentUDPRequestMessage request, InetAddress clientAddress) {
        ScrapeRequest scrapeRequest = (ScrapeRequest) request;
        List<Peer> peerList = HibernateUtil.list(Peer.class);
        boolean contains = false;
        Peer extract = null;
        for(Peer peer: peerList)
        {
            if(peer.getIdPeer().equals(scrapeRequest.getConnectionId())){
                contains = true;
                extract = peer;
                break;
            }
        }
        return scrapeRequest.getBytes().length >= 16 &&
                scrapeRequest.getInfoHashes()!=null &&
                scrapeRequest.getInfoHashes().size() > 0 &&
                scrapeRequest.getInfoHashes().size() <= 74 &&
                contains &&
                extract.getIp().equals(clientAddress.getHostAddress());
    }
    public byte[] getResponse(BitTorrentUDPRequestMessage request, InetAddress clientAddress, int clientPort) {
        ScrapeRequest scrapeRequest = (ScrapeRequest) request;
        ScrapeResponse scrapeResponse = new ScrapeResponse();
        scrapeResponse.setTransactionId(request.getTransactionId());
        List<String> infoHashes = scrapeRequest.getInfoHashes();
        ScrapeInfo scrapeInfo = null;
        for ( String infoHash : infoHashes ) {
            List<PeerSmarms> peerSmarmList = HibernateUtil.list(PeerSmarms.class);
            List<Smarms> smarmsList = HibernateUtil.list(PeerSmarms.class);
            scrapeInfo = sacarSeedersYLeechers(smarmsList, peerSmarmList, infoHash);
            scrapeResponse.addScrapeInfo(scrapeInfo);
            if ( scrapeResponse.getScrapeInfos().size() > 0 )
                return scrapeResponse.getBytes();
            else
                 return getError(request);
        }
        return null;
    }

    private ScrapeInfo sacarSeedersYLeechers(List<Smarms> smarmsList, List<PeerSmarms> peerSmarmList, String infoHash)
    {
        ScrapeInfo scrapeInfo = new ScrapeInfo();
        Smarms elegido = null;
        for(Smarms temp: smarmsList){
            if(temp.getSmarmsId().equals(infoHash)){
                elegido = temp;
                break;
            }
        }
        ArrayList<PeerSmarms> seeder = new ArrayList<>();
        ArrayList<PeerSmarms> leecher = new ArrayList<>();

        if(elegido!=null)
            for(PeerSmarms temp: peerSmarmList)
                if(temp.getSmarms().getSmarmsId().equals(elegido.getSmarmsId()))
                    if(temp.getBytesDescargados().equals(elegido.getTamanoEnBytes()))
                        seeder.add(temp);
                    else
                        leecher.add(temp);
        scrapeInfo.setCompleted(seeder.size());
        scrapeInfo.setSeeders(seeder.size());
        scrapeInfo.setLeechers(leecher.size());
        return scrapeInfo;
    }

    public byte[] getError(BitTorrentUDPRequestMessage request) {
        if(TrackerService.getInstance().getTracker().isMaster()) {
            Error error = new Error();
            error.setMessage("Error en el proceso de scrape");
            error.setTransactionId(request.getTransactionId());
            return error.getBytes();
        }
        return null;
    }
}
