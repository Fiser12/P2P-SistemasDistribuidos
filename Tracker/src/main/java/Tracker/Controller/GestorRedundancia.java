package Tracker.Controller;

import Tracker.Util.HibernateUtil;
import Tracker.VO.Estado;
import Tracker.VO.Tracker;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Fiser on 13/11/16.
 */
public class GestorRedundancia extends Observable implements Runnable, MessageListener {
    public boolean escuchandoPaquetes = true;
    public boolean pararHiloKeepAlive = false;
    public boolean esperandoID = false;
    public boolean pararComprobacionKeepAlive = false;
    public boolean eligiendoMaster = false;
    private ConcurrentHashMap<String, TrackerKeepAlive> trackersActivos;

    public GestorRedundancia()
    {
        trackersActivos = new ConcurrentHashMap<String, TrackerKeepAlive>();
    }

    @Override
    public void run()
    {
        JMSManager.getInstance().suscribir(this);
        hiloDeEnvioDeKeepAlive();
        hiloDeComprobarTrackersActivos();

        try {
            while (escuchandoPaquetes) {
                if (!eligiendoMaster) {
                    JMSManager.getInstance().startTopic();
                }
            }
        } catch (JMSException e) {
            System.err.println("Error en el bucle JMS");
        }

    }

    /**
     * Es el método que se va a encargar de enviar mensajes cada 2 segundos de KeepAlive a la red
     */
    private void hiloDeEnvioDeKeepAlive()
    {
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
    /**
     * Es el método que se va a encargar de comprobar todos los trackers de la red
     */
    private void hiloDeComprobarTrackersActivos()
    {
        Thread threadCheckKeepAliveMessages = new Thread() {
            public void run() {
                try {
                    Thread.sleep(8000);
                    eleccionDelMaster();
                    comprobarTrackersActivos();
                } catch (InterruptedException e) {
                    System.err.println("Error en el bucle ComprobarTrackers");
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

    /**
     * Recorreemos todos los trackers y comprobamos cuando es la última vez que han estado activos quitando de la lista los que se hayan caido
     */
    private void comprobarTrackersActivos()
    {
        Collection<TrackerKeepAlive> lista = trackersActivos.values();
        for (TrackerKeepAlive activeTracker : lista) {
            if (new Date().getTime() - activeTracker.getLastKeepAlive().getTime() >= 4000) {
                boolean master = activeTracker.isMaster();
                trackersActivos.remove(activeTracker.getId());
                if (master) {
                    eleccionDelMaster();
                }
            }
        }
    }
    private void eleccionDelMaster()
    {
        eligiendoMaster = true;
        boolean masterEncontrado = false;
        for (Map.Entry<String, TrackerKeepAlive> entry : trackersActivos.entrySet())
        {
            if(entry.getValue().getId()!=TrackerService.getInstance().getTracker().getId()) {
                if (entry.getValue().getId().compareTo(TrackerService.getInstance().getTracker().getId()) <= -1) {
                    TrackerService.getInstance().getTracker().setMaster(false);
                    masterEncontrado = true;
                    break;
                }
                if (entry.getValue().isMaster()) {
                    TrackerService.getInstance().getTracker().setMaster(false);
                    masterEncontrado = true;
                    break;
                }
            }
        }
        if(!masterEncontrado) {
            TrackerService.getInstance().getTracker().setMaster(true);
            crearNuevaBBDD();
        }
        eligiendoMaster = false;
    }
    private void calcularID(String id)
    {
        String candidateID = UUID.randomUUID().toString().replace("-", "");
        String tempID;
        for (TrackerKeepAlive activeTracker : trackersActivos.values()) {
            tempID = activeTracker.getId();
            if (tempID.compareTo(candidateID)>0) {
                candidateID = UUID.randomUUID().toString().replace("-", "");
            }
        }
        JMSManager.getInstance().enviarMensajeIdIncorrecto(id, String.valueOf(candidateID));
    }

    private void crearNuevaBBDD()
    {
        Session session = HibernateUtil.changeDatabase("jdbc:sqlite:tracker_"+TrackerService.getInstance().getTracker().getId()+".db").openSession();
        session.beginTransaction();
        session.getTransaction().commit();
    }
    private void convertirByteEnFichero(byte[] bytes)
    {
        File file = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
        file.delete();
        String newFileName = "tracker_" + TrackerService.getInstance().getTracker().getId() + ".db";
        File fileDest = new File(newFileName);
        FileOutputStream fileOutputStream = null;
        try {
            long length = fileDest.length();
            fileOutputStream = new FileOutputStream(fileDest);
            if (length > 0) {
                fileOutputStream.write((new String()).getBytes());
            }
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException");
        } catch (IOException e) {
            System.err.println("IOException");
        }
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
                    TrackerKeepAlive activeTracker = trackersActivos.get(id);
                    activeTracker.setLastKeepAlive(new Date());
                    activeTracker.setMaster(master);
                    trackersActivos.put(id, activeTracker);
                } else {
                    if (TrackerService.getInstance().getTracker().isMaster()) {
                        if (!id.equals(TrackerService.getInstance().getTracker().getId())) {
                            JMSManager.getInstance().enviarMensajeIdCorrecto(id);
                            JMSManager.getInstance().enviarBBDD(id);
                        }
                    }
                    TrackerKeepAlive activeTracker = new TrackerKeepAlive();
                    activeTracker.setConfirmacionActualizacion(Estado.Esperando);
                    activeTracker.setId(id);
                    activeTracker.setLastKeepAlive(new Date());
                    activeTracker.setMaster(master);
                    if (id.equals(TrackerService.getInstance().getTracker().getId()))
                        activeTracker.setiAm(true);
                    trackersActivos.put(activeTracker.getId(), activeTracker);
            }
            setChanged();
            notifyObservers(trackersActivos);
        }
    }

    public void idCorrecto(Object[] datos){
        if (((String) datos[0]).equals(TrackerService.getInstance().getTracker().getId()) && esperandoID) {
            esperandoID = false;
        }
    }
    public void idIncorrecto(Object[] datos){
        String idPropuesto = (String) datos[0];
        String idInicial = (String) datos[1];
        if (idInicial.equals(TrackerService.getInstance().getTracker().getId()) && esperandoID) {
            TrackerService.getInstance().getTracker().setId(idPropuesto);
            TrackerService.getInstance().getTracker().setMaster(false);
            esperandoID = false;
        }
    }

    /**
     * Este método se encarga de comprobar cada vez que recibe un ready de comprobar si ya están todos preparados y cuando son suficientes, confirma la update de la BBDD
     * @param datos
     */
    private void comprobarSiEstanPreparados(Object[] datos)
    {
        if (TrackerService.getInstance().getTracker().isMaster()) {

            String id = (String) datos[0];
            TrackerKeepAlive tracker = trackersActivos.get(id);
            tracker.setConfirmacionActualizacion(Estado.Confirmado);

            int confirmados = 0;
            for (TrackerKeepAlive trackerTemp : trackersActivos.values()) {
                if (trackerTemp.getConfirmacionActualizacion() == Estado.Confirmado) {
                    confirmados++;
                }
            }
            if (confirmados == trackersActivos.size()) {
                for (TrackerKeepAlive trackerTemp : trackersActivos.values()) {
                    trackerTemp.setConfirmacionActualizacion(Estado.Esperando);
                }
                /**
                 * TODO Guardar los nuevos datos en la BBDD
                 */
                JMSManager.getInstance().confirmacionActualizarBBDD();
            }
        }
    }

    /**
     * Coge los bytes recibidos con la nueva BBDD y la convierte en su base de datos y se conecta a ella
     * @param bytes
     */
    private void actualizarBBDD(byte[] bytes){
        convertirByteEnFichero(bytes);
        Session session = HibernateUtil.changeDatabase("jdbc:sqlite:tracker_"+TrackerService.getInstance().getTracker().getId()+".db").openSession();
        session.beginTransaction();
        session.getTransaction().commit();
    }
    @Override
    public void onMessage(Message mensaje)
    {
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
                        actualizarBBDD((byte[])data.toArray()[0]);
                        break;
                    case CorrectId:
                        idCorrecto(data.toArray());
                        break;
                    case IncorrectId:
                        idIncorrecto(data.toArray());
                        break;
                    case ReadyToStore:
                        comprobarSiEstanPreparados(data.toArray());
                        break;
                    case ConfirmToStore:
                        actualizarBBDD((byte[])data.toArray()[1]);
                        break;
                }
            } catch (JMSException e) {
                System.err.println("JMS Recibe mensaje error");
            }
        }
    }

}
