package Tracker.Util;

import Tracker.Controller.JMSManager;
import Tracker.Controller.TrackerService;
import Tracker.Model.Peer;
import Tracker.Model.Smarms;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class HibernateUtil {

    private static SessionFactory sessionFactory = buildSessionFactory();
    private static Session sessionElement;
    private static Query queryElement1;
    private static Query queryElement2;

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    private static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // Close caches and connection pools
        getSessionFactory().close();
    }
    public static SessionFactory changeDatabase(String url){
        Configuration cfg = new Configuration();
        cfg.configure();
        cfg.setProperty("hibernate.connection.url", url);
        sessionFactory = cfg.buildSessionFactory();
        return sessionFactory;
    }
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

    public static Object getData(String hash, Class clase)
    {
        for(Object temp: list(clase)){
            if(clase==Smarms.class){
                if(((Smarms)temp).getSmarmsId().equals(hash))
                    return temp;
            }else if(clase==Peer.class){
                long id = Long.parseLong(hash);
                if(((Peer)temp).getConnectionId()==id)
                    return temp;
            }
        }
        return null;
    }
    public static void saveData(Object obj){
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        session.save(obj);
        sessionElement = session;
        JMSManager.getInstance().solicitarCambioBBDD();
    }
    public static List list(Class c){
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        return session.createCriteria(c).list();
    }

    public static void eliminarSesiones() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        String hql = String.format("delete from Peer");
        queryElement1 = session.createQuery(hql);
        hql = String.format("delete from PeerSmarms");
        queryElement2 = session.createQuery(hql);
        JMSManager.getInstance().solicitarCambioBBDD();
    }
    public static void updateDatabase(){
        if(queryElement1!=null){
            queryElement1.executeUpdate();
            queryElement1 = null;
        }
        if(queryElement2!=null){
            queryElement2.executeUpdate();
            queryElement2 = null;
        }
        if(sessionElement!=null) {
            sessionElement.flush();
            sessionElement = null;
        }
    }
}