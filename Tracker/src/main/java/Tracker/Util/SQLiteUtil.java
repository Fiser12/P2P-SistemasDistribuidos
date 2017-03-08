package Tracker.Util;

import Tracker.Controller.JMSManager;
import Tracker.Controller.TrackerService;
import Tracker.Model.Peer;
import Tracker.Model.PeerSmarms;
import Tracker.Model.Smarms;
import Tracker.VO.TypeSQL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLiteUtil {
    private HashMap<String, String> stringHashMap = new HashMap<>();
    private HashMap<String, Object> objectHashMap = new HashMap<>();

    private static SQLiteUtil instance;
    private SQLiteUtil(){
    }
    public static SQLiteUtil getInstance(){
        if(instance==null)
            instance = new SQLiteUtil();
        return instance;
    }
    public void removeDatabase() {
        File file = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
        file.delete();
    }
    public byte[] getBytesDatabase(){

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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return bytes;
    }
    public void getDefaultDatabase(){
        File file = new File("trackerdb.db");
        File file2 = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
        try {
            Files.copy(file.toPath(), file2.toPath());
        } catch (IOException e) {
            removeDatabase();
            try {
                Files.copy(file.toPath(), file2.toPath());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    public<T> T getData(String hash, Class<T> clase)
    {
        if(clase == Peer.class)
            try {
                ArrayList<Peer> lista = ejecutarQuery("SELECT * FROM peer WHERE connectionId="+hash, TypeSQL.SELECT, Peer.class);
                if(!lista.isEmpty())
                    return (T)lista.get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        else if(clase == Smarms.class)
            try {
                ArrayList<Smarms> lista = ejecutarQuery("SELECT smarms.*, (SELECT COUNT(*) FROM peer_smarmses WHERE smarms.SMARMS_ID = peer_smarmses.SMARMS_ID) AS numberPeers FROM smarms WHERE SMARMS_ID='"+hash+"'", TypeSQL.SELECT, Smarms.class);
                if(!lista.isEmpty())
                    return (T)lista.get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return null;
    }
    public ArrayList<Peer> listPeer(){
        ArrayList<Peer> lista = new ArrayList<>();
        try {
            lista = ejecutarQuery("SELECT * FROM peer", TypeSQL.SELECT, Peer.class);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lista;
    }
    public ArrayList<Smarms> listSmarm(){
        ArrayList<Smarms> lista = new ArrayList<>();
        try {
            lista = ejecutarQuery("SELECT smarms.*, (SELECT COUNT(*) FROM peer_smarmses WHERE smarms.SMARMS_ID = peer_smarmses.SMARMS_ID) AS numberPeers FROM smarms", TypeSQL.SELECT, Smarms.class);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lista;
    }
    public ArrayList<PeerSmarms> listPeerSmarms(){
        ArrayList<PeerSmarms> lista = new ArrayList<>();
        try {
            lista = ejecutarQuery("SELECT peer.*, smarms.*, peer_smarmses.BYTES_DESCARGADOS FROM peer_smarmses, peer, smarms WHERE peer_smarmses.PEER_ID = peer.PEER_ID AND peer_smarmses.SMARMS_ID = smarms.SMARMS_ID", TypeSQL.SELECT, PeerSmarms.class);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lista;
    }
    public String getRandomStringID() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 15) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
    public void eliminarSesiones() {
        String idDelete = getRandomStringID();
        stringHashMap.put(idDelete,  "DELETE FROM peer_smarmses;DELETE FROM peer");
        JMSManager.getInstance().solicitarCambioBBDD(idDelete);
    }
    private Peer extractPeersFromResultSet(ResultSet result) throws SQLException {
        Peer peer = new Peer();
        peer.setIdPeer(result.getLong("PEER_ID"));
        peer.setConnectionId(result.getLong("connectionId"));
        peer.setIp(result.getString("PEER_IP"));
        peer.setPort(result.getInt("PEER_PORT"));
        return peer;
    }
    private Smarms extractSmarmsFromResultSet(ResultSet result) throws SQLException {
        Smarms smarms = new Smarms();
        smarms.setSmarmsId(result.getString("SMARMS_ID"));
        smarms.setHexInfoHash(result.getString("hexInfoHash"));
        smarms.setTamanoEnBytes(result.getInt("SMARMS_TAMANO"));
        smarms.setNumeroPares(result.getInt("numberPeers"));
        return smarms;
    }
    private PeerSmarms extractPeerSmarmsFromResultSet(ResultSet result) throws SQLException {
        PeerSmarms peerSmarms = new PeerSmarms();
        Peer peer = new Peer();
        peer.setIdPeer(result.getLong("PEER_ID"));
        peer.setConnectionId(result.getLong("connectionId"));
        peer.setIp(result.getString("PEER_IP"));
        peer.setPort(result.getInt("PEER_PORT"));
        Smarms smarms = new Smarms();
        smarms.setSmarmsId(result.getString("SMARMS_ID"));
        smarms.setHexInfoHash(result.getString("hexInfoHash"));
        smarms.setTamanoEnBytes(result.getInt("SMARMS_TAMANO"));
        peerSmarms.setPeer(peer);
        peerSmarms.setSmarms(smarms);
        peerSmarms.setBytesDescargados(result.getLong("BYTES_DESCARGADOS"));
        return peerSmarms;
    }
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public synchronized <T> ArrayList<T> ejecutarQuery(final String sql, final TypeSQL type, final Class<T> clase) throws SQLException {
        final ArrayList<T> lista = new ArrayList<>();
        executor.execute(new Runnable() {
            public void run() {
                Statement statement = null;
                Connection connect = null;
                ResultSet result = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    connect = DriverManager.getConnection("jdbc:sqlite:"+"tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
                    statement = connect.createStatement();
                    if(type==TypeSQL.SELECT) {
                        result = statement.executeQuery(sql);
                        while (result.next()) {
                            if (clase == Smarms.class) {
                                lista.add((T) extractSmarmsFromResultSet(result));
                            }
                            else if (clase == PeerSmarms.class) {
                                lista.add((T) extractPeerSmarmsFromResultSet(result));
                            }
                            else if (clase == Peer.class) {
                                lista.add((T) extractPeersFromResultSet(result));
                            }
                        }
                    }
                    else if(type==TypeSQL.CHANGE)
                        statement.executeUpdate(sql);
                } catch (SQLException e) {
                    System.err.println("ERR "+sql);
                    e.printStackTrace();
                } catch(ClassNotFoundException e){
                    e.printStackTrace();
                } finally {
                    synchronized (SQLiteUtil.this) {
                        SQLiteUtil.this.notify();
                    }
                    try {
                        if(result!=null)
                            result.close();
                        if(statement!=null)
                            statement.close();
                        if(connect!=null)
                            connect.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lista;
    }
    public void updateDatabase(String idDatabase){
        PreparedStatement st = null;
        try {
                ArrayList<String> listQuerys = new ArrayList(Arrays.asList(stringHashMap.get(idDatabase).split(";")));
                for(String temp: listQuerys) {
                    ejecutarQuery(temp, TypeSQL.CHANGE, Peer.class);
                }
                Object notify = objectHashMap.get(idDatabase);
                if(notify!=null)
                    synchronized (notify) {
                        notify.notify();
                    }
                stringHashMap.remove(idDatabase);
                objectHashMap.remove(idDatabase);
            } catch (SQLException e) {
                System.err.println(e.getMessage() + ": " + stringHashMap.get(idDatabase));
            }finally{
                if(st != null) {
                    try {
                        st.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
    }
    public void save(Smarms smarms, Object notify) throws NullPointerException{
        String id = getRandomStringID();
        stringHashMap.put(id, "INSERT INTO smarms (SMARMS_ID, hexInfoHash, SMARMS_TAMANO) values ('"+smarms.getHexInfoHash()+"','"+smarms.getHexInfoHash()+"',"+smarms.getTamanoEnBytes()+")");
        objectHashMap.put(id, notify);
        JMSManager.getInstance().solicitarCambioBBDD(id);
    }
    public void save(Peer peer, Object notify) throws NullPointerException{
        String id = getRandomStringID();
        stringHashMap.put(id, "INSERT INTO peer (PEER_ID, connectionId, PEER_IP, PEER_PORT) values ("+peer.getIdPeer()+","+peer.getConnectionId()+",'"+peer.getIp()+"',"+peer.getPort().intValue()+")");
        objectHashMap.put(id, notify);
        JMSManager.getInstance().solicitarCambioBBDD(id);
    }
    public void save(PeerSmarms peerSmarms, Object notify) throws NullPointerException{
        String id = getRandomStringID();
        stringHashMap.put(id, "INSERT INTO peer_smarmses (PEER_ID, SMARMS_ID, BYTES_DESCARGADOS) values ("+peerSmarms.getPeer().getIdPeer()+",'"+peerSmarms.getSmarms().getSmarmsId()+"',"+peerSmarms.getBytesDescargados()+")");
        objectHashMap.put(id, notify);
        JMSManager.getInstance().solicitarCambioBBDD(id);
    }
    public void saveData(Object obj, Object notify) throws NullPointerException{
        if(obj instanceof Smarms)
            save((Smarms)obj, notify);
        else if(obj instanceof Peer)
            save((Peer)obj, notify);
        else if(obj instanceof PeerSmarms)
            save((PeerSmarms)obj, notify);
    }
}