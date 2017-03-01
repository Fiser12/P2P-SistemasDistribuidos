package Peer.test;

import java.io.IOException;

/**
 * Created by Fiser on 1/3/17.
 */
public class test {
    public static void main(String [] args) throws IOException {
        PeerClient peerClient = new PeerClient();
        peerClient.start();
    }
}
