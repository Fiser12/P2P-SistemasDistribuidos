package Tracker.Controller;

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
            System.err.println("NamingException (publishKeepAliveMessage) ");
        } catch (JMSException e) {
            System.err.println("JMS Exception (publishKeepAliveMessage) ");
        }
        try {
            queueConnectionFactory = (QueueConnectionFactory) context2.lookup("QueueConnectionFactory");
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueTrackersManagement = (Queue) context2.lookup("jndi.ssdd.trackersmanagement");
            queueSender = queueSession.createSender(queueTrackersManagement);
        } catch (NamingException e) {
            System.err.println("NamingException (publishKeepAliveMessage) ");
        } catch (JMSException e) {
            System.err.println("JMS Exception (publishKeepAliveMessage) ");
        }
    }
    public static JMSManager getInstance(){
        if (instance == null) {
            instance = new JMSManager();
        }
        return instance;
    }
    // TOPIC
    public void enviarMensajeKeepAlive() {
        try {
            if (instance != null) {
                Topic topicKeepAliveMessages = (Topic) context1.lookup("jndi.ssdd.keepalivemessages");
                TopicPublisher topicPublisher = topicSession.createPublisher(topicKeepAliveMessages);
                topicPublishers.add(topicPublisher);
                MapMessage mapMessage = topicSession.createMapMessage();
                mapMessage.setStringProperty("TypeMessage", "KeepAlive");
                mapMessage.setString("Id", TrackerService.getInstance().getTracker().getId());
                mapMessage.setBoolean("Master", TrackerService.getInstance().getTracker().isMaster());
                topicPublisher.publish(mapMessage);
            }
        } catch (JMSException e) {
            System.err.println("JMS Exception (publishKeepAliveMessage) ");
        } catch (NamingException e) {
            System.err.println("NamingException (publishKeepAliveMessage) ");
        }

    }
    public void enviarMensajeDeIdIncorrecto(String originId, String candidateId){
        try {
            if (instance != null) {
                Topic topicIncorrectIdMessages = (Topic) context1
                        .lookup("jndi.ssdd.incorrectidmessages");

                TopicPublisher topicPublisher = topicSession
                        .createPublisher(topicIncorrectIdMessages);
                topicPublishers.add(topicPublisher);
                // Map Message
                MapMessage mapMessage = topicSession.createMapMessage();

                // Message Properties
                mapMessage.setStringProperty("TypeMessage", "TYPE_ERROR_ID_MESSAGE"); //TODO: Pensar si definir las constantes por separado

                // Message Body
                mapMessage.setString("OriginId", originId);
                mapMessage.setString("CandidateId", candidateId);

                topicPublisher.publish(mapMessage);
                System.out.println("- MapMessage sent to the Topic!");
            }
        } catch (JMSException e) {
            System.err
                    .println("# JMS Exception Error (publishIncorrectIdMessage) "
                            + e.getMessage());
        } catch (NamingException e) {
            System.err
                    .println("# Name Exception Error (publishIncorrectIdMessage) "
                            + e.getMessage());
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
}
