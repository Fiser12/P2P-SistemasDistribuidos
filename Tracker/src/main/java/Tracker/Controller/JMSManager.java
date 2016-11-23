package Tracker.Controller;

import Tracker.Util.UtilController;
import Tracker.VO.TypeMessage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fiser on 13/11/16.
 */
public class JMSManager {
    private static JMSManager instance = null;
    private List<TopicPublisher> topicPublishers = null;
    private List<TopicSubscriber> topicSubscribers = null;
    private Context context1 = null;
    private Context context2 = null;

    private TopicConnection topicConnection = null;
    private TopicSession topicSession = null;
    private TopicConnectionFactory topicConnectionFactory = null;

    private QueueConnectionFactory queueConnectionFactory;
    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private Queue queueTrackersManagement;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;

    private JMSManager() {
        topicPublishers = new ArrayList<TopicPublisher>();
        topicSubscribers = new ArrayList<TopicSubscriber>();
        try {
            context1 = new InitialContext();
            topicConnectionFactory = (TopicConnectionFactory) context1.lookup("TopicConnectionFactory");
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (NamingException e) {
            System.err.println("NamingException - JMSManager Constructor 1");
            e.printStackTrace();
        } catch (JMSException e) {
            System.err.println("JMS Exception - JMSManager Constructor 1");
            e.printStackTrace();
        }
        try {
            context2 = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory) context2.lookup("QueueConnectionFactory");
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueTrackersManagement = (Queue) context2.lookup("jndi.ssdd.trackersmanagement");
            queueSender = queueSession.createSender(queueTrackersManagement);
        } catch (NamingException e) {
            System.err.println("NamingException - JMSManager Constructor 2");
            e.printStackTrace();
        } catch (JMSException e) {
            System.err.println("JMS Exception - JMSManager Constructor 2");
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
            lista.add(UtilController.JNDIIncorrectId);
            lista.add(UtilController.JNDICorrectId);
            //TODO: Suscribir solo a los que interesa

            if (instance != null) {
                for(String add: lista) {
                    Topic topicMensajes = (Topic) context1.lookup(add);
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
    public void recibirMensajesParaMiId(GestorRedundancia gestor){
        try {
            if (instance != null) {
                queueReceiver = queueSession.createReceiver(queueTrackersManagement, "Destino = '" + TrackerService.getInstance().getTracker().getId() + "'");
                queueReceiver.setMessageListener(gestor);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - Recibir Mensajes para mi id");
        }
    }

    public void enviarMensajeKeepAlive() {
        try {
            if (instance != null) {
                Topic topicKeepAliveMessages = (Topic) context1.lookup(UtilController.JNDINameKeepAlive);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicKeepAliveMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.KeepAlive.toString());
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                mapMessage.setBoolean("Master", TrackerService.getInstance().getTracker().isMaster());
                topicPublisher.publish(mapMessage);
                System.out.println("KEEPALIVE");
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - KeepAlive");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - KeepAlive");
            e.printStackTrace();
        }

    }
    public void enviarMensajeIdIncorrecto(String idInicial, String idPropuesto) {
        try {
            if (instance != null) {
                Topic topicIncorrectIdMessages = (Topic) context1.lookup(UtilController.JNDIIncorrectId);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicIncorrectIdMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.IncorrectId.toString());
                mapMessage.setString("IdInicial", idInicial);
                mapMessage.setString("IdPropuesto", idPropuesto);
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception - IncorrectID");
            e.printStackTrace();
        } catch (NamingException e) {
            System.err.println("NamingException - IncorrectID");
            e.printStackTrace();
        }
    }
    public void enviarMensajeIdCorrecto(String idDestino) {
        try {
            if (instance != null) {
                Topic topicIncorrectIdMessages = (Topic) context1.lookup(UtilController.JNDICorrectId);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicIncorrectIdMessages);
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
        if (instance != null) {
            File file = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
            byte[] bytes = null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
                bytes = bos.toByteArray();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MapMessage mapMessage;
            try {
                mapMessage = queueSession.createMapMessage();
                mapMessage.setStringProperty("Type", TypeMessage.BackUp.toString());
                mapMessage.setStringProperty("Destino", destino);
                mapMessage.setString("Id", destino);
                mapMessage.setBytes("file", bytes);
                queueSender.send(mapMessage);
            } catch (JMSException e) {
                System.err.println("JMS Exception - enviarBBDD");
            }
        }
    }

    public void startTopic() throws JMSException {
        topicConnection.start();
    }
    public void startQueue() throws JMSException {
        queueConnection.start();
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
        } catch (JMSException e) {

        }
        try {
            if (queueSender != null)
                queueSender.close();
            if (queueReceiver != null)
                queueReceiver.close();
            if (queueSession != null)
                queueSession.close();
            if (queueConnection != null)
                queueConnection.close();
        } catch (JMSException e) {

        }

    }
}
