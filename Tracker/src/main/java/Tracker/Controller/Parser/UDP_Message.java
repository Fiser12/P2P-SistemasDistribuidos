package Tracker.Controller.Parser;

import Tracker.Util.bittorrent.tracker.protocol.udp.BitTorrentUDPRequestMessage;

import java.net.InetAddress;

public interface UDP_Message {
    BitTorrentUDPRequestMessage parse(byte[] data);
    boolean validate(BitTorrentUDPRequestMessage request, InetAddress clientAddress);
    byte[] sendResponse(BitTorrentUDPRequestMessage request, InetAddress clientAddress, int clientPort);
    byte[] sendError(BitTorrentUDPRequestMessage request, String error);
}
