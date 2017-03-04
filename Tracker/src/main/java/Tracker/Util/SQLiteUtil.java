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

    private static String sql1;
    private static String sql2;

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
        close();
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
        close();
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
        close();
        return lista;
    }

    public static void eliminarSesiones() {
        sql1 = "DELETE FROM peer_smarmses";
        sql2 = "DELETE FROM peer";
        JMSManager.getInstance().solicitarCambioBBDD();
    }

    public static void updateDatabase(){
        if(sql1 !=null) {
            try {
                connect();
                PreparedStatement ps = connect.prepareStatement(sql1);
                ps.execute();
                System.out.println(sql1);
                sql1 = null;
                close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("ERR:"+sql1);

            }
        }
        if(sql2 !=null) {
            try {
                connect();
                PreparedStatement ps = connect.prepareStatement(sql2);
                ps.execute();
                System.out.println(sql2);
                sql2 = null;
                close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("ERR:"+sql2);

            }
        }
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
        sql1 = "insert into smarms (SMARMS_ID, hexInfoHash, SMARMS_TAMANO) values ('"+smarms.getHexInfoHash()+"','"+smarms.getHexInfoHash()+"',"+smarms.getTamanoEnBytes()+")";
    }
    public static void save(Peer peer){
        sql1 = "insert into peer (PEER_ID, connectionId, PEER_IP, PEER_PORT) values ("+peer.getIdPeer()+","+peer.getConnectionId()+",'"+peer.getIp()+"',"+peer.getPort().intValue()+")";
    }
    public static void save(PeerSmarms peerSmarms){
        sql1 = "insert into peer_smarmses (PEER_ID, SMARMS_ID, BYTES_DESCARGADOS) values ("+peerSmarms.getPeer().getIdPeer()+",'"+peerSmarms.getSmarms().getSmarmsId()+"',"+peerSmarms.getBytesDescargados()+")";
    }
    public static void saveData(Object obj){
        if(obj instanceof Smarms)
            save((Smarms)obj);
        else if(obj instanceof Peer)
            save((Peer)obj);
        else if(obj instanceof PeerSmarms)
            save((PeerSmarms)obj);
        JMSManager.getInstance().solicitarCambioBBDD();
    }
}