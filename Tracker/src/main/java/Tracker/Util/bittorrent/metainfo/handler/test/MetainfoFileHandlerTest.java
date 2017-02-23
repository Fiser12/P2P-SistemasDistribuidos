package Tracker.Util.bittorrent.metainfo.handler.test;

import Tracker.Util.bittorrent.metainfo.handler.MetainfoHandler;
import Tracker.Util.bittorrent.metainfo.handler.MetainfoHandlerMultipleFile;
import Tracker.Util.bittorrent.metainfo.handler.MetainfoHandlerSingleFile;

import java.io.File;

public class MetainfoFileHandlerTest {
    public static void main(String[] args) {
        try {
            File folder = new File("torrent");
            MetainfoHandler<?> handler = null;

            if (folder.isDirectory()) {
                for (File torrent : folder.listFiles()) {
                    try {
                        if (torrent.getPath().contains(".torrent")) {
                            handler = new MetainfoHandlerSingleFile();
                            handler.parseTorrenFile(torrent.getPath());
                        }
                    } catch (Exception ex) {
                        if (torrent.getPath().contains(".torrent")) {
                            handler = new MetainfoHandlerMultipleFile();
                            handler.parseTorrenFile(torrent.getPath());
                        }
                    }

                    if (handler != null) {
                        System.out.println("#######################################\n" + torrent.getPath());
                        System.out.println(handler.getMetainfo());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("# MetainforFileHandlerTest: " + ex.getMessage());
        }
    }
}