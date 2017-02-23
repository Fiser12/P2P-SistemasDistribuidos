package Tracker.Util.bittorrent.tracker.protocol.udp;

import Tracker.Util.bittorrent.util.TorrentUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Offset      	Size            	Name            Value
 * 0           	32-bit integer  	action          2 // scrape
 * 4           	32-bit integer  	transaction_id
 * 8 + 12 * n  	32-bit integer  	seeders
 * 12 + 12 * n 	32-bit integer  	completed
 * 16 + 12 * n 	32-bit integer  	leechers
 * 8 + 12 * N
 */

public class ScrapeResponse extends BitTorrentUDPMessage {

    private List<ScrapeInfo> scrapeInfos;

    public ScrapeResponse() {
        super(Action.SCRAPE);
        this.scrapeInfos = new ArrayList<>();
    }



    @Override
    public byte[] getBytes() {
        //TODO Revisar
        int initialSize = 8;
        int scrapeInfoSize = 12;

        int messageSize = initialSize + scrapeInfoSize * scrapeInfos.size();
        ByteBuffer byteBuffer = ByteBuffer.allocate(messageSize);

        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        byteBuffer.putInt(0, getAction().value());
        byteBuffer.putInt(4, getTransactionId());
        int inicio = initialSize;
        for (ScrapeInfo scrapeInfo : scrapeInfos) {
            byteBuffer.putInt(inicio, scrapeInfo.getSeeders());
            inicio += TorrentUtils.INT_SIZE;
            byteBuffer.putInt(inicio, scrapeInfo.getCompleted());
            inicio += TorrentUtils.INT_SIZE;
            byteBuffer.putInt(inicio, scrapeInfo.getLeechers());
            inicio += TorrentUtils.INT_SIZE;
        }
        byteBuffer.flip();

        return byteBuffer.array();
    }

    public static ScrapeResponse parse(byte[] byteArray) {
        //TODO Revisar
        int initialSize = 8;
        int scrapeInfoSize = 12;

        ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
        ScrapeResponse scrapeResponse = new ScrapeResponse();
        scrapeResponse.setAction(Action.valueOf(bufferReceive.getInt(0)));
        scrapeResponse.setTransactionId(bufferReceive.getInt(4));

        int index;
        for (index = initialSize; index < byteArray.length; index += scrapeInfoSize) {
            //get message info
            int seeders = bufferReceive.getInt(index);
            int completed = bufferReceive.getInt(index + 4);
            int leechers = bufferReceive.getInt(index + 8);

            //build object
            ScrapeInfo scrapeInfo = new ScrapeInfo();
            scrapeInfo.setSeeders(seeders);
            scrapeInfo.setCompleted(completed);
            scrapeInfo.setLeechers(leechers);

            //add to list
            scrapeResponse.addScrapeInfo(scrapeInfo);
        }
        //return object
        return scrapeResponse;

    }
    public List<ScrapeInfo> getScrapeInfos() {
        return scrapeInfos;
    }

    public void addScrapeInfo(ScrapeInfo scrapeInfo) {
        if (scrapeInfo != null && !this.scrapeInfos.contains(scrapeInfo)) {
            this.scrapeInfos.add(scrapeInfo);
        }
    }

    public void setScrapeInfos(List<ScrapeInfo> info){
        if(info!=null){
            this.scrapeInfos = info;
        }
    }
}