package Tracker.View;

import Tracker.Controller.TrackerService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Fiser on 8/12/16.
 */
public class TrackerMain {
    private JTextField ipTextField;
    private IntegerField portTextField;
    private IntegerField portPeersTextField;
    private JButton Start;
    private JPanel toolbar;
    private JTabbedPane tabbedPaneTrackers;
    private JPanel bottom;
    private JTable tableTrackers;
    private JTable tableSmarms;
    private JPanel mainPanel;
    private IntegerField textFieldId;
    private static JFrame trackerWindow;

    public TrackerMain() {
        textFieldId.setText("1");
        portPeersTextField.setText("2000");
        portTextField.setText("3000");
        Start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Start.getText().equals("Start")){
                    Start.setText("Stop");
                    TrackerService.getInstance().connect(ipTextField.getText(), portTextField.getNumber(), portPeersTextField.getNumber(), textFieldId.getText());
                }
                else {
                    Start.setText("Start");
                    TrackerService.getInstance().disconnect();
                }
            }
        });
    }
    public static void launchWindow(){
        trackerWindow = new JFrame("Tracker");
        trackerWindow.setContentPane(new TrackerMain().mainPanel);
        trackerWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        trackerWindow.pack();
        trackerWindow.setSize(700, 500);
        trackerWindow.setMinimumSize(new Dimension(600, 400));
        trackerWindow.setVisible(true);
    }
}
