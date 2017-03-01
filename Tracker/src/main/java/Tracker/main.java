package Tracker;

import Tracker.Controller.TrackerService;

public class main {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        TrackerService trackerService = TrackerService.getInstance();
        trackerService.ejecutarVentana();
    }
}
