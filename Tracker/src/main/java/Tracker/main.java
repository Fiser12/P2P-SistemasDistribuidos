package Tracker;

import java.util.Date;

import Tracker.Model.Peer;
import Tracker.Model.PeerSmarms;
import Tracker.Model.Smarms;
import Tracker.Util.HibernateUtil;
import org.hibernate.Session;

public class main {
    public static void main(String[] args) {
        System.out.println("Hibernate many to many - join table + extra column (Annotation)");
        Session session = HibernateUtil.getSessionFactory().openSession();

        session.beginTransaction();

        Smarms smarms = new Smarms();
        smarms.setName("j");
        smarms.setTamanoEnBytes(23);
        Peer peer = new Peer();
        peer.setIp("asdf");
        peer.setPort(43233);
        session.save(peer);


        PeerSmarms peerSmarms = new PeerSmarms();
        peerSmarms.setBytesDescargados(88);
        peerSmarms.setSmarms(smarms);
        smarms.getPeerSmarmses().add(peerSmarms);
        peer.getPeerSmarmses().add(peerSmarms);
        session.save(smarms);
        session.getTransaction().commit();
        System.out.println("Done");
    }
}
