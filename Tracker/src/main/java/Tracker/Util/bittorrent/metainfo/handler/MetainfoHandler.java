package Tracker.Util.bittorrent.metainfo.handler;

import Tracker.Util.bittorrent.bencoding.Bencoder;
import Tracker.Util.bittorrent.metainfo.InfoDictionary;
import Tracker.Util.bittorrent.metainfo.MetainfoFile;
import Tracker.Util.bittorrent.util.ByteUtils;
import Tracker.Util.bittorrent.util.StringUtils;

import java.util.HashMap;
import java.util.List;

public abstract class MetainfoHandler<Info extends InfoDictionary> {
    private Bencoder bencoder;
    private MetainfoFile<Info> metainfo;

    public MetainfoHandler() {
        this.bencoder = new Bencoder();
        this.metainfo = new MetainfoFile<Info>();
    }

    protected Bencoder getBencoder() {
        return this.bencoder;
    }

    public MetainfoFile<Info> getMetainfo() {
        return this.metainfo;
    }

    protected void setMetainfo(MetainfoFile<Info> metainfo) {
        this.metainfo = metainfo;
    }

    @SuppressWarnings("unchecked")
    public void parseTorrenFile(String filename) {
        byte[] fileBytes = ByteUtils.fileToByteArray(filename);

        HashMap<String, Object> dictionary = bencoder.unbencodeDictionary(fileBytes);

        if (dictionary.containsKey("announce")) {
            this.metainfo.setAnnounce((String) dictionary.get("announce"));
        }

        if (dictionary.containsKey("announce-list")) {
            this.metainfo.setAnnounceList(((List<List<String>>) dictionary.get("announce-list")));
        }

        if (dictionary.containsKey("creation date")) {
            this.metainfo.setCreationDate((Integer) dictionary.get("creation date"));
        }

        if (dictionary.containsKey("comment")) {
            this.metainfo.setComment((String) dictionary.get("comment"));
        }

        if (dictionary.containsKey("created by")) {
            this.metainfo.setComment((String) dictionary.get("created by"));
        }

        if (dictionary.containsKey("encoding")) {
            this.metainfo.setComment((String) dictionary.get("encoding"));
        }

        this.parseInfo((HashMap<String, Object>) dictionary.get("info"));

        byte[] infoHash = bencoder.generateHash(fileBytes, "4:info");

        if (infoHash != null) {
            this.metainfo.getInfo().setInfoHash(infoHash);
            this.metainfo.getInfo().setUrlInfoHash(StringUtils.toURLEncodedString(infoHash));
            this.metainfo.getInfo().setHexInfoHash(StringUtils.toHexString(infoHash));
        }
    }

    protected abstract void parseInfo(HashMap<String, Object> info);

    protected void parsePieces(String piecesString) {
        if (piecesString.length() % 20 != 0) {
            System.err.println("# [MetainfoSingleFileHandler]: Length of the SHA-1 hash for the file's pieces is incorrect.");
            return;
        }

        byte[] stringBytes = piecesString.getBytes();
        byte[] individualHash;
        int numPieces = piecesString.length() / 20;

        for (int i = 0; i < numPieces; i++) {
            individualHash = new byte[20];

            for (int j = 0; j < 20; j++) {
                individualHash[j] = stringBytes[(20 * i) + j];
            }

            this.metainfo.getInfo().getByteSHA1().add(individualHash);
            this.metainfo.getInfo().getHexStringSHA1().add(StringUtils.toHexString(individualHash));
            this.metainfo.getInfo().getUrlEncodedSHA1().add(StringUtils.toURLEncodedString(individualHash));
        }
    }
}