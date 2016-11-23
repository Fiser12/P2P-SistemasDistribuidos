package Tracker.Controller;

import Tracker.Model.Peer;
import Tracker.Model.PeerSmarms;
import Tracker.Model.Smarms;
import Tracker.Util.HibernateUtil;
import Tracker.VO.Tracker;
import Tracker.View.TrackerWindow;
import org.hibernate.Session;

/**
 * Created by Fiser on 21/10/16.
 */
public class TrackerService {
    TrackerWindow ventana;
    private static TrackerService instance;
    private Tracker tracker;
    private GestorRedundancia gestorRedundancia = new GestorRedundancia();

    private TrackerService(){
        ventana = new TrackerWindow();
        tracker = new Tracker();
    }
    public static TrackerService getInstance() {
        if (instance == null) {
            instance = new TrackerService();
        }
        return instance;
    }

    public void ejecutarVentana(){
        ventana.launch();
    }
    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }


    public void connect(String ipAddress, int port, int portForPeers, String id) {
        gestorRedundancia.escuchandoPaquetes = true;
        gestorRedundancia.pararHiloKeepAlive = false;
        gestorRedundancia.pararComprobacionKeepAlive = false;
        tracker.setId(id);
        tracker.setPort(port);
        tracker.setPortForPeers(portForPeers);
        tracker.setIpAddress(ipAddress);
        tracker.setMaster(false);
        new Thread(gestorRedundancia).start();

    }
    public static void testBBDD()
    {
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
