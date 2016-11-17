package Tracker.Controller;

import Tracker.Util.HibernateUtil;
import Tracker.VO.TrackerKeepAlive;
import Tracker.VO.TypeMessage;
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
    public boolean pararEscucharPaquetes = false;
    public boolean pararHiloKeepAlive = false;
    public boolean pararComprobacionKeepAlive = false;
    public boolean esperandoATenerID = true;
    public boolean eligiendoMaster = false;

    public GestorRedundancia() {
        trackers = new HashMap<String, Boolean>();
    }

    @Override
    public void run() {
        JMSManager.getInstance().suscribir(this);
        hiloDeEnvioDeKeepAlive();
        hiloDeComprobarTrackersActivos();

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
                        //eleccionDelMaster();
                        comprobarTrackersActivos();
                    }
                } catch (InterruptedException e1) {

                }

                while (!pararComprobacionKeepAlive) {
                    try {
                        Thread.sleep(8000);
                        comprobarTrackersActivos();
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
        threadCheckKeepAliveMessages.start();
    }
    private void comprobarTrackersActivos()
    {

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

    private void calcularID(String id) {

    }

    private void crearNuevaBBDD() {
        Session session = HibernateUtil.changeDatabase("jdbc:sqlite:tracker_"+TrackerService.getInstance().getTracker().getId()+".db").openSession();
        session.beginTransaction();
        session.getTransaction().commit();
    }

    private void sincronizarBBDD(Object[] data) {
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

    private TypeMessage tipoMensaje(MapMessage message) {
        Enumeration<String> propertyNames;
        String typeMessage = "";
        try {
            propertyNames = (Enumeration<String>) message.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement();
                if (propertyName.equals("Type")) {
                    typeMessage = message.getStringProperty(propertyName);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return TypeMessage.valueOf(typeMessage);
    }

    @Override
    public void onMessage(Message mensaje) {
        if(mensaje!=null && mensaje.getClass().getCanonicalName().equals(ActiveMQMapMessage.class.getCanonicalName())){
            MapMessage mapMensaje = ((MapMessage) mensaje);
            TypeMessage tipoMensaje = tipoMensaje(mapMensaje);
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
                switch(tipoMensaje){
                    case KeepAlive:
                        System.out.println("RECIBIDO");
                        break;
                    case BackUp:

                        break;
                    case ConfirmToStore:

                        break;
                    case CorrectId:

                        break;
                    case IncorrectId:

                        break;
                    case ReadyToStore:

                        break;
                }
            } catch (JMSException e) {
                System.err.println("JMS Recibe mensaje error");
            }
        }
    }

}
