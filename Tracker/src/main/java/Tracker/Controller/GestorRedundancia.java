package Tracker.Controller;

import Tracker.Util.HibernateUtil;
import Tracker.Util.UtilController;
import Tracker.VO.TrackerKeepAlive;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.hibernate.Session;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Fiser on 13/11/16.
 */
public class GestorRedundancia implements Runnable, MessageListener {
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
        JMSManager.getInstance().suscribir(this);
        try {
            while (!pararEscucharPaquetes) {
                if (!eligiendoMaster) {
                    JMSManager.getInstance().startTopic();
                    JMSManager.getInstance().startQueue();
                }
            }
        } catch (JMSException e) {
            System.err.println("Error en el bucle JMS");
        }

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

                }

                while (!pararComprobacionKeepAlive) {
                    try {
                        Thread.sleep(8000);
                        checkActiveTrackers();
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
        threadCheckKeepAliveMessages.start();
    }

    private void eleccionDelMaster() {
        eligiendoMaster = true;
        HashMap<String, TrackerKeepAlive> mapActiveTrackers = TrackerService.getInstance().getTracker().getTrackersActivos();
        if (mapActiveTrackers.size() == 0 || (mapActiveTrackers.size() == 1 && mapActiveTrackers.containsKey(TrackerService.getInstance().getTracker().getId()))) {
            TrackerService.getInstance().getTracker().setMaster(true);
            if (esperandoATenerID) {
                esperandoATenerID = false;
                crearNuevaBBDD();
                JMSManager.getInstance().recibirMensajesParaMiId(this);
            }

        } else {
            boolean enc = false;
            Integer i = 0;
            List<String> keysMapActiveTrackers = new ArrayList<String>(mapActiveTrackers.keySet());
            while (!enc && i < mapActiveTrackers.values().size()) {
                TrackerKeepAlive activeTracker = mapActiveTrackers.get(keysMapActiveTrackers.get(i));
                if (activeTracker != null) {
                    if (activeTracker.getId().compareTo(TrackerService.getInstance().getTracker().getId()) <= -1) {
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
        Session session = HibernateUtil.changeDatabase("jdbc:sqlite:tracker_"+TrackerService.getInstance().getTracker().getId()+".db").openSession();
        session.beginTransaction();
        session.getTransaction().commit();
    }

    private void sincronizarBBDD(Object... data) {
        byte[] bytesOfDbFile = (byte[]) data[0];
        String newFileName = "tracker_" + TrackerService.getInstance().getTracker().getId() + ".db";
        File fileDest = new File(newFileName);
        FileOutputStream file = null;
        try {
            long length = fileDest.length();
            file = new FileOutputStream(fileDest);
            if (length > 0) {
                file.write((new String()).getBytes());
            }
            file.write(bytesOfDbFile);
            file.flush();
            file.close();
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException");
        } catch (IOException e) {
            System.err.println("IOException");
        }

        Session session = HibernateUtil.changeDatabase("jdbc:sqlite:tracker_"+TrackerService.getInstance().getTracker().getId()+".db").openSession();
        session.beginTransaction();
        session.getTransaction().commit();
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
    private void keepAliveRecibido(Object... data) {
        boolean master = (Boolean) data[0];
        String id = (String) data[1];
        HashMap<String, TrackerKeepAlive> activeTrackers = TrackerService.getInstance().getTracker().getTrackersActivos();
        if (!esperandoATenerID) {
            if (activeTrackers.containsKey(id)) {
                if (id.equals(TrackerService.getInstance().getTracker().getId()) && enviarKeepAlive) {
                    enviarKeepAlive = false;
                } else if (id.equals(TrackerService.getInstance().getTracker().getId()) && !enviarKeepAlive) {
                    calcularID(id);
                }

                if (TrackerService.getInstance().getTracker().getId().equals(id)) {
                    if (TrackerService.getInstance().getTracker().isMaster() == master) {
                        TrackerKeepAlive activeTracker = activeTrackers.get(id);
                        activeTracker.setLastKeepAlive(new Date());
                        activeTracker.setMaster(master);
                        notifyObservers(new String("EditActiveTracker"));
                    }
                }

                if (!TrackerService.getInstance().getTracker().getId().equals(id)) {
                    TrackerKeepAlive activeTracker = activeTrackers.get(id);
                    activeTracker.setLastKeepAlive(new Date());
                    activeTracker.setMaster(master);
                    notifyObservers(new String("EditActiveTracker"));
                }
            } else {
                boolean continuar = true;
                if (TrackerService.getInstance().getTracker().isMaster()) {
                    if (id.compareTo(TrackerService.getInstance().getTracker().getId()) <= -1 || (id.equals(TrackerService.getInstance().getTracker().getId()) && !master)) {
                        continuar = false;
                    } else {
                        if (!id.equals(TrackerService.getInstance().getTracker().getId())) {
                            JMSManager.getInstance().publishCorrectIdMessage(id);
                            JMSManager.getInstance().mensajeDeBackup(id);
                        }
                    }
                }
                if (continuar) {
                    TrackerKeepAlive activeTracker = new TrackerKeepAlive();
                    activeTracker.setActive(true);
                    activeTracker.setId(id);
                    activeTracker.setLastKeepAlive(new Date());
                    activeTracker.setMaster(master);
                    TrackerService.getInstance().getTracker().addActiveTracker(activeTracker);
                    notifyObservers(new String("NewActiveTracker"));
                } else {
                    if (!master) {
                        calcularID(id);
                    } else {

                    }
                }
            }
        }
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
                if (tipoMensaje.equals(UtilController.TYPE_KEEP_ALIVE_MESSAGE)) {
                    keepAliveRecibido(data.toArray());
                } else if (tipoMensaje.equals(UtilController.TYPE_READY_TO_STORE_MESSAGE) && TrackerService.getInstance().getTracker().isMaster()) {
                   // checkIfAllAreReadyToStore(data.toArray());
                } else if (tipoMensaje.equals(UtilController.TYPE_CONFIRM_TO_STORE_MESSAGE)) {
                   // storeTemporalData( data.toArray() );
                } else if (tipoMensaje.equals(UtilController.TYPE_ERROR_ID_MESSAGE)) {
                   // checkErrorIDMessage(data.toArray());
                } else if (tipoMensaje.equals(UtilController.TYPE_CORRECT_ID_MESSAGE)) {
                   // checkIfCorrectBelongsToTracker(data.toArray());
                } else if (tipoMensaje.equals(UtilController.TYPE_BACKUP_MESSAGE)) {
                    sincronizarBBDD(data.toArray());
                }
            } catch (JMSException e) {
                System.err.println("JMS Recibe mensaje error");
            }
        }
    }

}
