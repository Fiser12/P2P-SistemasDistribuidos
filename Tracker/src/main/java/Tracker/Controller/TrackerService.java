package Tracker.Controller;

import Tracker.View.TrackerWindow;

/**
 * Created by Fiser on 21/10/16.
 */
public class TrackerService {
    TrackerWindow ventana;
    public TrackerService(){
        ventana = new TrackerWindow();
    }
    public void ejecutarVentana(){
        ventana.launch();
    }
}
