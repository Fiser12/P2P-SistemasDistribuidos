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
    public boolean escuchandoPaquetes = true;
    public boolean pararHiloKeepAlive = false;
    public boolean esperandoID = true;
    public boolean pararComprobacionKeepAlive = false;
    public boolean eligiendoMaster = false;
    private HashMap<String, TrackerKeepAlive> trackersActivos;

    public GestorRedundancia() {
        trackersActivos = new HashMap<String, TrackerKeepAlive>();
    }

    @Override
    public void run() {
        JMSManager.getInstance().suscribir(this);
        hiloDeEnvioDeKeepAlive();
        hiloDeComprobarTrackersActivos();

        try {
            while (escuchandoPaquetes) {
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
                        Thread.sleep(2000);
                        JMSManager.getInstance().enviarMensajeKeepAlive();
                    } catch (InterruptedException e) {
                        System.err.println("Error envio KeepAlive");
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
                    Thread.sleep(4000);
                    if (!pararComprobacionKeepAlive) {
                        eleccionDelMaster();
                        comprobarTrackersActivos();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Error en la comprobación inicial de trackers");
                }
                while (!pararComprobacionKeepAlive) {
                    try {
                        Thread.sleep(4000);
                        comprobarTrackersActivos();
                    } catch (InterruptedException e) {
                        System.err.println("Error en el bucle ComprobarTrackers");
                    }
                }
            }
        };
        threadCheckKeepAliveMessages.start();
    }
    private void comprobarTrackersActivos()
    {
        for (TrackerKeepAlive activeTracker : trackersActivos.values()) {
            if (new Date().getTime() - activeTracker.getLastKeepAlive().getTime() >= 4000) {
                boolean master = activeTracker.isMaster();
                trackersActivos.remove(activeTracker.getId());
                if (master) {
                    eleccionDelMaster();
                }
            }
        }

    }
    private void eleccionDelMaster() {
        eligiendoMaster = true;
        if (trackersActivos.size() == 0) {
            TrackerService.getInstance().getTracker().setMaster(true);
            crearNuevaBBDD();
            JMSManager.getInstance().recibirMensajesParaMiId(this);
        }
        else{
            boolean masterEncontrado = false;
            for (Map.Entry<String, TrackerKeepAlive> entry : trackersActivos.entrySet())
            {
                if (entry.getValue().getId().compareTo(TrackerService.getInstance().getTracker().getId()) <= -1) {
                    TrackerService.getInstance().getTracker().setMaster(false);
                    masterEncontrado = true;
                    break;
                }
            }
            if(!masterEncontrado)
                TrackerService.getInstance().getTracker().setMaster(true);
        }
        eligiendoMaster = false;
    }
    private void calcularID(String id) {
        int candidateID = Integer.parseInt(TrackerService.getInstance().getTracker().getId()) + 1;
        int tempID;
        for (TrackerKeepAlive activeTracker : trackersActivos.values()) {
            tempID = Integer.parseInt(activeTracker.getId());
            if (tempID == candidateID) {
                candidateID++;
            }
        }
        JMSManager.getInstance().enviarMensajeIdIncorrecto(id, String.valueOf(candidateID));
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

    public void keepAlive(Object[] datos){
        boolean master = (Boolean) datos[0];
        String id = (String) datos[1];
        if (!esperandoID) {
            if (trackersActivos.containsKey(id)) {
                if (!TrackerService.getInstance().getTracker().getId().equals(id)) {
                    TrackerKeepAlive activeTracker = trackersActivos.get(id);
                    activeTracker.setLastKeepAlive(new Date());
                    activeTracker.setMaster(master);
                }
            } else {
                if (TrackerService.getInstance().getTracker().isMaster()&& id.compareTo(TrackerService.getInstance().getTracker().getId()) <= -1 && !master) {
                    if (!id.equals(TrackerService.getInstance().getTracker().getId())) {
                        JMSManager.getInstance().enviarMensajeIdCorrecto(id);
                        JMSManager.getInstance().enviarBBDD(id);
                    }
                    TrackerKeepAlive activeTracker = new TrackerKeepAlive();
                    activeTracker.setActive(true);
                    activeTracker.setId(id);
                    activeTracker.setLastKeepAlive(new Date());
                    activeTracker.setMaster(master);
                    trackersActivos.put(activeTracker.getId(), activeTracker);
                } else {
                    if (!master) {
                        calcularID(id);
                    }
                }
            }
        }
    }
    public void idCorrecto(Object[] datos){
        if (((String) datos[0]).equals(TrackerService.getInstance().getTracker().getId()) && esperandoID) {
            esperandoID = false;
            JMSManager.getInstance().recibirMensajesParaMiId(this);
        }
    }
    public void idIncorrecto(Object[] datos){
        String idPropuesto = (String) datos[0];
        String idInicial = (String) datos[1];
        if (idInicial.equals(TrackerService.getInstance().getTracker().getId()) && esperandoID) {
            TrackerService.getInstance().getTracker().setId(idPropuesto);
            esperandoID = false;
            JMSManager.getInstance().recibirMensajesParaMiId(this);
        }
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
                        keepAlive(data.toArray());
                        break;
                    case BackUp:
                        sincronizarBBDD(data.toArray());
                        break;
                    case CorrectId:
                        idCorrecto(data.toArray());
                        break;
                    case IncorrectId:
                        idIncorrecto(data.toArray());
                        break;
                    case ConfirmToStore:

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
