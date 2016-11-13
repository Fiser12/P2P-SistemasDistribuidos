package Tracker;

import Tracker.Controller.TrackerService;
import Tracker.Model.Peer;
import Tracker.Model.PeerSmarms;
import Tracker.Model.Smarms;
import Tracker.Util.HibernateUtil;
import org.hibernate.Session;
import Tracker.View.TrackerWindow;

public class main {
    public static void main(String[] args) {
        TrackerService controller = TrackerService.getInstance();
        controller.ejecutarVentana();
    }
}
