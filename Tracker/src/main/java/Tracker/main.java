package Tracker;

import Tracker.Controller.TrackerService;

public class main {
    public static void main(String[] args) {
        TrackerService controller = TrackerService.getInstance();
        controller.ejecutarVentana();
    }
}
