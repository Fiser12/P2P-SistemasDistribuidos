package Tracker;

import Tracker.Controller.TrackerService;

public class main {
    public static void main(String[] args) {
        TrackerService trackerService = TrackerService.getInstance();
        trackerService.ejecutarVentana();
    }
}
