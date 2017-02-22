package Tracker.View;

import Tracker.Controller.TrackerService;
import Tracker.VO.TrackerKeepAlive;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Fiser on 8/12/16.
 */
public class TrackerMain extends JFrame implements Observer {
    private JTextField ipTextField;
    private IntegerField portTextField;
    private IntegerField portPeersTextField;
    private JButton Start;
    private JPanel toolbar;
    private JPanel bottom;
    private JTabbedPane tabbedPaneTrackers;
    private JTable tableTrackers;
    private JTable tableSmarms;
    private JPanel mainPanel;
    private IntegerField textFieldId;
    private JPanel smarmsPanel;
    private JPanel trackersPanel;
    private DefaultTableModel tableModel;
    private String[] column_names_trackers= {"Id","Es Master","Ultima update", "Estado"};
    DefaultTableModel table_model_trackers=new DefaultTableModel(column_names_trackers,0);
    String column_names_smarms[]= {"Id","Tamaño en bytes","Pares conectados"};
    DefaultTableModel table_model_smarms=new DefaultTableModel(column_names_smarms,0);

    public TrackerMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(700, 500);
        setMinimumSize(new Dimension(700, 400));

        tableTrackers=new JTable(table_model_trackers);
        trackersPanel.add(tableTrackers.getTableHeader(), BorderLayout.NORTH);
        trackersPanel.add(tableTrackers, BorderLayout.CENTER);
        tableSmarms=new JTable(table_model_smarms);
        smarmsPanel.add(tableSmarms.getTableHeader(), BorderLayout.NORTH);
        smarmsPanel.add(tableSmarms, BorderLayout.CENTER);

        textFieldId.setText("1");
        portPeersTextField.setText("2000");
        portTextField.setText("3000");
        Start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Start.getText().equals("Start")){
                    Start.setText("Stop");
                    TrackerService.getInstance().connect(ipTextField.getText(), portTextField.getNumber(), portPeersTextField.getNumber());
                }
                else {
                    Start.setText("Start");
                    TrackerService.getInstance().disconnect();
                }
            }
        });
        setContentPane(mainPanel);
    }

    public synchronized void actualizarInterfaz(ConcurrentHashMap<String, TrackerKeepAlive> valores){
        DefaultTableModel defaultTable=new DefaultTableModel(column_names_trackers, 0);

        for(Map.Entry<String, TrackerKeepAlive> activeTracker : valores.entrySet()) {
            String[] temp = {activeTracker.getValue().getId(), (activeTracker.getValue().isMaster())?"Si":"No", activeTracker.getValue().getLastKeepAlive().toString(), activeTracker.getValue().getConfirmacionActualizacion().toString() };
            defaultTable.addRow(temp);
        }
        tableTrackers.setModel(defaultTable);
        tableTrackers.revalidate();
        tableTrackers.repaint();
    }

    @Override
    public void update(Observable o, Object arg) {
        actualizarInterfaz((ConcurrentHashMap<String, TrackerKeepAlive>)arg);
    }
}
