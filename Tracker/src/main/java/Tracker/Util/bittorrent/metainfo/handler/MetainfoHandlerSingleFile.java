package Tracker.Util.bittorrent.metainfo.handler;

import Tracker.Util.bittorrent.metainfo.InfoDictionarySingleFile;

import java.util.HashMap;

public class MetainfoHandlerSingleFile extends MetainfoHandler<InfoDictionarySingleFile> {

    public MetainfoHandlerSingleFile() {
        super();
    }

    protected void parseInfo(HashMap<String, Object> info) {
        InfoDictionarySingleFile infoDictionary = new InfoDictionarySingleFile();
        super.getMetainfo().setInfo(infoDictionary);

        if (info.containsKey("length")) {
            infoDictionary.setLength((Integer) info.get("length"));
        } else {
            super.setMetainfo(null);

            return;
        }

        if (info.containsKey("piece length")) {
            infoDictionary.setPieceLength((Integer) info.get("piece length"));
        }

        if (info.containsKey("pieces")) {
            super.parsePieces((String) info.get("pieces"));
        }

        if (info.containsKey("private")) {
            infoDictionary.setPrivatePeers((Integer) info.get("private"));
        }

        if (info.containsKey("name")) {
            infoDictionary.setName((String) info.get("name"));
        }
    }
}