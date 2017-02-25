package Tracker.Controller;

import Tracker.Util.HibernateUtil;
import Tracker.Util.UtilController;
import Tracker.VO.TypeMessage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fiser on 13/11/16.
 */
public class JMSManager {
    private static JMSManager instance = null;
    private List<TopicPublisher> topicPublishers = null;
    private List<TopicSubscriber> topicSubscribers = null;
    private Context context = null;

    private TopicConnection topicConnection = null;
    private TopicSession topicSession = null;
    private TopicConnectionFactory topicConnectionFactory = null;


    private JMSManager() {
        topicPublishers = new ArrayList<>();
        topicSubscribers = new ArrayList<>();
        try {
            context = new InitialContext();
            topicConnectionFactory = (TopicConnectionFactory) context.lookup("TopicConnectionFactory");
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (NamingException e) {
            System.err.println("NamingException - JMSManager Constructor 1");
            e.printStackTrace();
        } catch (JMSException e) {
            System.err.println("JMS Exception - JMSManager Constructor 1");
            e.printStackTrace();
        }
    }
    public static JMSManager getInstance(){
        if (instance == null) {
            instance = new JMSManager();
        }
        return instance;
    }

    /**
     * El GestorDeRedundancia pasará a recibir los mensajes con los JNDI a los que se le suscribe
     * @param suscribir
     */
    public void suscribir(GestorRedundancia suscribir)
    {
        try {
            ArrayList<String> lista = new ArrayList<String>();
            lista.add(UtilController.JNDINameKeepAlive);
            lista.add(UtilController.JNDIReadyToStore);
            lista.add(UtilController.JNDIConfirmToStore);
            lista.add(UtilController.JNDICorrectId);
            lista.add(UtilController.JNDISendBBDD);
            lista.add(UtilController.JNDISoliciteChangeBBDD);

            if (instance != null) {
                for(String add: lista) {
                    Topic topicMensajes = (Topic) context.lookup(add);
                    TopicSubscriber topicSubscriber = topicSession.createSubscriber(topicMensajes);
                    topicSubscribers.add(topicSubscriber);
                    topicSubscriber.setMessageListener(suscribir);
                }
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - Suscribir");
        } catch (NamingException e) {
            System.err.println("NamingException - Suscribir");
        }
    }
    public void enviarMensajeKeepAlive() {
        try {
            if (instance != null) {
                Topic topicKeepAliveMessages = (Topic) context.lookup(UtilController.JNDINameKeepAlive);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicKeepAliveMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.KeepAlive.toString());
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                mapMessage.setBoolean("Master", TrackerService.getInstance().getTracker().isMaster());
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - KeepAlive");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - KeepAlive");
            e.printStackTrace();
        }
    }
    public void enviarMensajeIdCorrecto(String idDestino) {
        try {
            if (instance != null) {
                Topic topicCorrectIdMessages = (Topic) context.lookup(UtilController.JNDICorrectId);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicCorrectIdMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage;
                mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.CorrectId.toString());
                mapMessage.setString("Id", idDestino);
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - CorrectId");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - CorrectId");
            e.printStackTrace();
        }
    }
    public void enviarBBDD(String destino) {
            try {
                if (instance != null) {
                    Topic topicEnviarBBDD = (Topic) context.lookup(UtilController.JNDISendBBDD);
                    TopicPublisher topicPublisher = topicSession.createPublisher(topicEnviarBBDD);
                    topicPublishers.add(topicPublisher);
                    MapMessage mapMessage;
                    mapMessage = topicSession.createMapMessage();
                    mapMessage.setStringProperty("Type", TypeMessage.BackUp.toString());
                    mapMessage.setString("Id", destino);
                    mapMessage.setBytes("file", HibernateUtil.getBytesDatabase());
                    topicPublisher.publish(mapMessage);
                }
            } catch (JMSException e) {
                System.err.println("JMS Exception - CorrectId");
                e.printStackTrace();
            } catch (NamingException e) {
                System.err.println("NamingException - CorrectId");
                e.printStackTrace();
            }
    }
    public void solicitarCambioBBDD() {
        try {
            if (instance != null) {
                Topic topicConfirm = (Topic) context.lookup(UtilController.JNDISoliciteChangeBBDD);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicConfirm);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.SolicitaCambioBBDD.toString());
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - Manda mensaje para guardar");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - Manda mensaje para guardar");
            e.printStackTrace();
        }
    }
    public void confirmacionListoParaGuardar() {
        try {
            if (instance != null) {
                Topic topicReady = (Topic) context.lookup(UtilController.JNDIReadyToStore);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicReady);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.ReadyToStore.toString());
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                mapMessage.setBoolean("Listo", true);
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - Manda mensaje para guardar");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - Manda mensaje para guardar");
            e.printStackTrace();
        }
    }
    public void rechazoListoParaGuardar() {
        try {
            if (instance != null) {
                Topic topicReady = (Topic) context.lookup(UtilController.JNDIReadyToStore);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicReady);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.ReadyToStore.toString());
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                mapMessage.setBoolean("Listo", false);
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - Manda mensaje para guardar");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - Manda mensaje para guardar");
            e.printStackTrace();
        }
    }
    public void confirmacionActualizarBBDD() {
        try {
            if (instance != null) {
                Topic topicConfirm = (Topic) context.lookup(UtilController.JNDIConfirmToStore);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicConfirm);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.ConfirmToStore.toString());
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                mapMessage.setBytes("file", HibernateUtil.getBytesDatabase());
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - Manda mensaje para guardar");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - Manda mensaje para guardar");
            e.printStackTrace();
        }
    }
    public void startTopic() throws JMSException {
        topicConnection.start();
    }
    public void close() {
        try {
            for (TopicSubscriber topicSubscriber : topicSubscribers) {
                topicSubscriber.close();
            }
            for (TopicPublisher topicPublisher : topicPublishers) {
                topicPublisher.close();
            }
            if (topicSession != null)
                topicSession.close();
            if (topicConnection != null)
                topicConnection.close();
        } catch (JMSException ignored) {

        }
    }
}
