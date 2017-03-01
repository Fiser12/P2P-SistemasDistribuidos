package Tracker.Util;

import Tracker.Controller.JMSManager;
import Tracker.Controller.TrackerService;
import Tracker.Model.Peer;
import Tracker.Model.PeerSmarms;
import Tracker.Model.Smarms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;

public class SQLiteUtil {

    private static PreparedStatement st;
    private static PreparedStatement st2;

    private static Connection connect;


    public static void removeDatabase() {
        File file = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
        file.delete();
    }
    public static byte[] getBytesDatabase(){

        File file = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
        byte[] bytes = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
            bytes = bos.toByteArray();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
    public static void getDefaultDatabase(){

        File file = new File("trackerdb.db");
        File file2 = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
        try {
            Files.copy(file.toPath(), file2.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object getData(String hash, Class clase)
    {
        if(clase == Peer.class)
            for(Peer temp: listPeer()){
                long id = Long.parseLong(hash);
                if(((Peer)temp).getConnectionId()==id)
                    return temp;
            }
        else if(clase == Smarms.class)
            for(Smarms temp: listSmarm()){
                if(((Smarms)temp).getHexInfoHash().equals(hash))
                    return temp;
            }
        return null;
    }
    public static ArrayList<Peer> listPeer(){
        connect();
        ArrayList<Peer> lista = new ArrayList<>();
        ResultSet result = null;
        try {
            PreparedStatement st = connect.prepareStatement("select * from peer");
            result = st.executeQuery();
            while (result.next()) {
                Peer peer = new Peer();
                peer.setIdPeer(result.getLong("PEER_ID"));
                peer.setConnectionId(result.getLong("connectionId"));
                peer.setIp(result.getString("PEER_IP"));
                peer.setPort(result.getInt("PEER_PORT"));
                lista.add(peer);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return lista;
    }
    public static ArrayList<Smarms> listSmarm(){
        connect();
        ArrayList<Smarms> lista = new ArrayList<>();
        ResultSet result = null;
        try {
            PreparedStatement st = connect.prepareStatement("select * from smarms");
            result = st.executeQuery();
            while (result.next()) {
                Smarms smarms = new Smarms();
                smarms.setSmarmsId(result.getString("SMARMS_ID"));
                smarms.setHexInfoHash(result.getString("hexInfoHash"));
                smarms.setTamanoEnBytes(result.getInt("SMARMS_TAMANO"));
                lista.add(smarms);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return lista;
    }
    public static ArrayList<PeerSmarms> listPeerSmarms(){
        connect();
        ArrayList<PeerSmarms> lista = new ArrayList<>();
        ResultSet result = null;
        try {
            PreparedStatement st = connect.prepareStatement("select * from peer_smarmses");
            result = st.executeQuery();
            while (result.next()) {
                PeerSmarms peerSmarms = new PeerSmarms();
                peerSmarms.setPeer((Peer)getData(String.valueOf(result.getLong("PEER_ID")), Peer.class));
                peerSmarms.setSmarms((Smarms)getData(result.getString("SMARMS_ID"), Smarms.class));
                peerSmarms.setBytesDescargados(result.getLong("BYTES_DESCARGADOS"));
                lista.add(peerSmarms);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return lista;
    }

    public static void eliminarSesiones() {
        connect();
        try {
            st = connect.prepareStatement("DELETE FROM peer_smarmses");
            st2 = connect.prepareStatement("DELETE FROM peer");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        JMSManager.getInstance().solicitarCambioBBDD();
    }

    public static void updateDatabase(){
        if(st!=null) {
            try {
                st.execute();
                st = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(st2!=null) {
            try {
                st2.execute();
                st2 = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        close();
    }

    public static void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connect = DriverManager.getConnection("jdbc:sqlite:"+"tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
        }catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void close(){
        try {
            connect.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void save(Smarms smarms){
        try {
            st = connect.prepareStatement("insert into smarms (SMARMS_ID, hexInfoHash, SMARMS_TAMANO) values (?,?,?)");
            st.setString(1, smarms.getHexInfoHash());
            st.setString(2, smarms.getHexInfoHash());
            st.setInt(3, smarms.getTamanoEnBytes());
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    public static void save(Peer peer){
        try {
            st = connect.prepareStatement("insert into peer (PEER_ID, connectionId, PEER_IP, PEER_PORT) values (?,?,?,?)");
            st.setLong(1, peer.getConnectionId());
            st.setLong(2, peer.getConnectionId());
            st.setString(3, peer.getIp());
            st.setInt(4, peer.getPort().intValue());
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    public static void save(PeerSmarms peerSmarms){
        try {
            st = connect.prepareStatement("insert into peer_smarmses (PEER_ID, SMARMS_ID, BYTES_DESCARGADOS) values (?,?,?)");
            st.setLong(1, peerSmarms.getPeer().getIdPeer());
            st.setString(2, peerSmarms.getSmarms().getSmarmsId());
            st.setLong(3, peerSmarms.getBytesDescargados());
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    public static void saveData(Object obj){
        connect();
        if(obj instanceof Smarms)
            save((Smarms)obj);
        else if(obj instanceof Peer)
            save((Peer)obj);
        else if(obj instanceof PeerSmarms)
            save((PeerSmarms)obj);
        JMSManager.getInstance().solicitarCambioBBDD();
    }
}