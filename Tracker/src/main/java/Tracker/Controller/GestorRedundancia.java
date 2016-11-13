package Tracker.Controller;

import Tracker.VO.TrackerKeepAlive;
import org.apache.activemq.command.ActiveMQMapMessage;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.*;

/**
 * Created by Fiser on 13/11/16.
 */
public class GestorRedundancia implements Runnable, MessageListener {
    public static String PATH_DDBB = "trackerdb.db";
    private HashMap<String, Boolean> trackers;
    private List<Observer> observers;
    private boolean pararEscucharPaquetes = false;
    private boolean pararHiloKeepAlive = false;
    private boolean pararComprobacionKeepAlive = false;
    private boolean enviarKeepAlive;
    private boolean esperandoATenerID = true;
    private boolean eligiendoMaster = false;

    public GestorRedundancia() {
        observers = new ArrayList<Observer>();
        trackers = new HashMap<String, Boolean>();
    }

    public void run() {
        hiloDeEnvioDeKeepAlive();
        hiloDeComprobarTrackersActivos();
    }

    private void hiloDeEnvioDeKeepAlive() {
        Thread threadSendKeepAliveMessages = new Thread() {
            public void run() {
                while (!pararHiloKeepAlive) {
                    try {
                        Thread.sleep(4000);
                        enviarKeepAlive = true;
                        JMSManager.getInstance().enviarMensajeKeepAlive();
                    } catch (InterruptedException e) {
                        System.err.println("**INTERRUPTED EXCEPTION..."
                                + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        };
        threadSendKeepAliveMessages.start();
    }

    private void hiloDeComprobarTrackersActivos() {
        Thread threadCheckKeepAliveMessages = new Thread() {

            public void run() {
                try {
                    Thread.sleep(8000);
                    if (!pararComprobacionKeepAlive) {
                        eleccionDelMaster();
                        checkActiveTrackers();
                    }
                } catch (InterruptedException e1) {
                    System.err.println("** INTERRUPTED EXCEPTION: "
                            + e1.getMessage());
                }

                while (!pararComprobacionKeepAlive) {
                    try {
                        Thread.sleep(8000);
                        checkActiveTrackers();
                    } catch (InterruptedException e) {
                        System.err.println("** INTERRUPTED EXCEPTION: "
                                + e.getMessage());
                    }
                }
            }
        };
        threadCheckKeepAliveMessages.start();
    }

    private void eleccionDelMaster() {
        System.out.println("Start electing the new master");
        eligiendoMaster = true;
        HashMap<String, TrackerKeepAlive> mapActiveTrackers = TrackerService.getInstance().getTracker().getTrackersActivos();
        if (mapActiveTrackers.size() == 0
                || (mapActiveTrackers.size() == 1 && mapActiveTrackers
                .containsKey(TrackerService.getInstance().getTracker().getId()))) {
            System.out
                    .println("Only exists one active tracker and I am this one, so "
                            + TrackerService.getInstance().getTracker().getId() + "is the new master");
            TrackerService.getInstance().getTracker().setMaster(true);
            if (esperandoATenerID) {
                esperandoATenerID = false;
                crearNuevaBBDD();
                JMSManager.getInstance().recibirMensajesParaMiId(this);
            }

        } else {
            boolean enc = false;
            Integer i = 0;
            List<String> keysMapActiveTrackers = new ArrayList<String>(
                    mapActiveTrackers.keySet());
            System.out.println("Active Trackers to compare with "
                    + keysMapActiveTrackers);
            while (!enc && i < mapActiveTrackers.values().size()) {
                TrackerKeepAlive activeTracker = (TrackerKeepAlive) mapActiveTrackers.get(keysMapActiveTrackers.get(i));
                System.out.println("Active tracker >> " + activeTracker);
                if (activeTracker != null) {
                    System.out.println("Para caso tracker "
                            + activeTracker.getId()
                            + " comparamos "
                            + activeTracker.getId().compareTo(
                            TrackerService.getInstance().getTracker().getId()));
                    if (activeTracker.getId().compareTo(TrackerService.getInstance().getTracker().getId()) <= -1) {
                        System.out
                                .println("Found an active tracker with a id less than mine ("
                                        + TrackerService.getInstance().getTracker().getId()
                                        + ") that is "
                                        + activeTracker.getId());
                        TrackerService.getInstance().getTracker().setMaster(false);
                        enc = true;
                    }
                }
                i++;
            }
            if (!enc) {
                TrackerService.getInstance().getTracker().setMaster(true);
            }
        }
        eligiendoMaster = false;

    }

    private void checkActiveTrackers() {
        for (TrackerKeepAlive activeTracker : TrackerService.getInstance().getTracker().getTrackersActivos()
                .values()) {
            long time = activeTracker.getLastKeepAlive().getTime();
            long actualTime = new Date().getTime();
            if (actualTime - time >= 8000) {

                System.out.println("Deleting the tracker "
                        + activeTracker.getId() + " ...");
                boolean isMaster = activeTracker.isMaster();
                TrackerService.getInstance().getTracker().getTrackersActivos().remove(activeTracker.getId());
                if (isMaster) {
                    eleccionDelMaster();
                } else {
                    this.notifyObservers(new String("DeleteActiveTracker"));
                }

            }
        }
    }

    private void calcularID(String id) {
        int candidateID = Integer.parseInt(TrackerService.getInstance().getTracker().getId()) + 1;
        int tempID;
        List<TrackerKeepAlive> orderedList = TrackerService.getInstance().obtenerTrackersActivos();
        Collections.sort(orderedList, new TrackerKeepAlive());
        for (TrackerKeepAlive activeTracker : orderedList) {
            tempID = Integer.parseInt(activeTracker.getId());
            if (tempID == candidateID) {
                candidateID++;
            } else if (candidateID < tempID) {
                break;
            }
        }
        JMSManager.getInstance().enviarMensajeDeIdIncorrecto(id,String.valueOf(candidateID));

    }

    private void crearNuevaBBDD() {
        /*
        TODO Crear el código que genera nuestra base de datos con hibernate y con un nombre en funcion de su ID
         */
    }

    public void refresh() {

    }

    public void addObserver(Observer o) {
        if (o != null && !this.observers.contains(o)) {
            this.observers.add(o);
        }
    }

    public void deleteObserver(Observer o) {
        this.observers.remove(o);
    }

    public void notifyObservers(Object param) {
        for (Observer observer : this.observers) {
            if (observer != null) {
                observer.update(null, param);
            }
        }
    }

    public void desconectar() {
        this.notifyObservers(null);
    }
    private String tipoMensaje(MapMessage message) {
        Enumeration<String> propertyNames;
        String typeMessage = "";
        try {
            propertyNames = (Enumeration<String>) message.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement();
                if (propertyName.equals("TypeMessage")) {
                    typeMessage = message.getStringProperty(propertyName);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return typeMessage;
    }

    @Override
    public void onMessage(Message mensaje) {
        if(mensaje!=null && mensaje.getClass().getCanonicalName().equals(ActiveMQMapMessage.class.getCanonicalName())){
            MapMessage mapMensaje = ((MapMessage) mensaje);
            String tipoMensaje = tipoMensaje(mapMensaje);
            try {
                Enumeration<String> mapKeys = (Enumeration<String>) mapMensaje.getMapNames();
                String key = null;
                List<Object> data = new ArrayList<Object>();
                while (mapKeys.hasMoreElements()) {
                    key = mapKeys.nextElement();
                    if (key != null & !key.equals("")) {
                        data.add(mapMensaje.getObject(key));
                    }
                }
                /*
                TODO: aquí van nuestros mensajes para otdas las acciones posibles
                if (tipoMensaje.equals(Constants.TYPE_KEEP_ALIVE_MESSAGE)) {
                    saveActiveTracker(data.toArray());
                } else if (tipoMensaje.equals(Constants.TYPE_READY_TO_STORE_MESSAGE)) {
                    if (getTracker().isMaster()) {
                        checkIfAllAreReadyToStore(data.toArray());
                    }
                } else if (tipoMensaje.equals(Constants.TYPE_CONFIRM_TO_STORE_MESSAGE)) {
                    storeTemporalData( data.toArray() );
                } else if (tipoMensaje.equals(Constants.TYPE_ERROR_ID_MESSAGE)) {
                    checkErrorIDMessage(data.toArray());
                } else if (tipoMensaje.equals(Constants.TYPE_CORRECT_ID_MESSAGE)) {
                    checkIfCorrectBelongsToTracker(data.toArray());
                } else if (tipoMensaje.equals(Constants.TYPE_BACKUP_MESSAGE)) {
                    generateDatabaseForPeersAndTorrents(data.toArray());
                }
                */
            } catch (JMSException e) {
                System.err.println("JMS Recibe mensaje error");
            }
        }
    }
}
