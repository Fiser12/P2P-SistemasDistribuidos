package Tracker;

import Tracker.Controller.TrackerService;

public class main {
    public static void main(String[] args) {
        TrackerService controller = TrackerService.getInstance();
//        controller.ejecutarVentana();
        controller.connect("224.0.0.4", 1140, 1150, "76");
    }
}
