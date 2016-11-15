package Tracker.Controller;

import Tracker.Util.UtilController;

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
            topicConnectionFactory = (TopicConnectionFactory) context1
                    .lookup("TopicConnectionFactory");
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicSession = topicConnection.createTopicSession(false,
                    Session.AUTO_ACKNOWLEDGE);
        } catch (NamingException e) {
            System.err.println("NamingException");
        } catch (JMSException e) {
            System.err.println("JMS Exception");
        }
        try {
            queueConnectionFactory = (QueueConnectionFactory) context2.lookup("QueueConnectionFactory");
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueTrackersManagement = (Queue) context2.lookup("jndi.ssdd.trackersmanagement");
            queueSender = queueSession.createSender(queueTrackersManagement);
        } catch (NamingException e) {
            System.err.println("NamingException");
        } catch (JMSException e) {
            System.err.println("JMS Exception");
        }
    }
    public static JMSManager getInstance(){
        if (instance == null) {
            instance = new JMSManager();
        }
        return instance;
    }
    public void suscribir(GestorRedundancia suscribir)
    {
        try {
            ArrayList<String> lista = new ArrayList<String>();
            lista.add(UtilController.JNDINameKeepAlive);
            lista.add(UtilController.JNDIReadyToStore);
            lista.add(UtilController.JNDIConfirmToStore);
            lista.add(UtilController.JNDIIncorrectId);
            lista.add(UtilController.JNDICorrectId);

            if (instance != null) {
                for(String add: lista) {
                    Topic topicMensajes = (Topic) context1.lookup(add);
                    TopicSubscriber topicSubscriber = topicSession.createSubscriber(topicMensajes);
                    topicSubscribers.add(topicSubscriber);
                    topicSubscriber.setMessageListener(suscribir);
                }
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception");
        } catch (NamingException e) {
            System.err.println("NamingException");
        }

    }
    public void enviarMensajeKeepAlive() {
        try {
            if (instance != null) {
                Topic topicKeepAliveMessages = (Topic) context1.lookup(UtilController.JNDINameKeepAlive);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicKeepAliveMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("TypeMessage", "KeepAlive");
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                mapMessage.setBoolean("Master", TrackerService.getInstance().getTracker().isMaster());
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception");
        } catch (NamingException e) {
            System.err.println("NamingException");
        }

    }
    public void enviarMensajeDeIdIncorrecto(String originId, String candidateId){
        try {
            if (instance != null) {
                Topic topicIncorrectIdMessages = (Topic) context1.lookup(UtilController.JNDIIncorrectId);
                TopicPublisher topicPublisher = topicSession
                        .createPublisher(topicIncorrectIdMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("TypeMessage", "TYPE_ERROR_ID_MESSAGE");
                mapMessage.setString("OriginId", originId);
                mapMessage.setString("CandidateId", candidateId);

                topicPublisher.publish(mapMessage);
                System.out.println("- MapMessage sent to the Topic!");
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception");
        } catch (NamingException e) {
            System.err.println("NamingException");
        }
    }
    public void recibirMensajesParaMiId(GestorRedundancia gestor){
        try {
            if (instance != null) {
                queueReceiver = queueSession.createReceiver(queueTrackersManagement, "DestinationId = '" + TrackerService.getInstance().getTracker().getId() + "'");
                queueReceiver.setMessageListener(gestor);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception (publishKeepAliveMessage) ");
        }
    }
    public void publishCorrectIdMessage(String destinationId) {
        try {
            if (instance != null) {
                Topic topicConfirmToStoreMessages = (Topic) context1.lookup(UtilController.JNDICorrectId);
                TopicPublisher topicPublisher = topicSession.createPublisher(topicConfirmToStoreMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage;
                mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("TypeMessage", UtilController.TYPE_CORRECT_ID_MESSAGE);
                mapMessage.setStringProperty("DestinationId", destinationId);
                mapMessage.setString("Id", destinationId);
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception");
        } catch (NamingException e) {
            System.err.println("NamingException");
        }
    }
    public void mensajeDeBackup(String destinationId) {
        if (instance != null) {
            File file = new File("tracker_" + TrackerService.getInstance().getTracker().getId() + ".db");
            byte[] bytes = null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
                bytes = bos.toByteArray();
                fis.close();
            } catch (IOException e) {
            }
            MapMessage mapMessage;
            try {
                mapMessage = queueSession.createMapMessage();
                mapMessage.setStringProperty("TypeMessage", UtilController.TYPE_BACKUP_MESSAGE);
                mapMessage.setStringProperty("DestinationId", destinationId);
                mapMessage.setString("Id", destinationId);
                mapMessage.setBytes("file", bytes);
                queueSender.send(mapMessage);
            } catch (JMSException e) {
            }
        }
    }
    public void startTopic() throws JMSException {
        topicConnection.start();
    }
    public void startQueue() throws JMSException {
        queueConnection.start();
    }
    public void closeTopic() {
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
    }
    public void closeQueue() {
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
