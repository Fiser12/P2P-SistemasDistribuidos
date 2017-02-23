package Tracker.View;

import Tracker.Controller.JMSManager;
import Tracker.Controller.TrackerService;
import Tracker.Util.HibernateUtil;
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
    private JPanel smarmsPanel;
    private JPanel trackersPanel;
    private String[] column_names_trackers= {"Id","Es Master","Ultima update", "Estado"};
    private DefaultTableModel table_model_trackers=new DefaultTableModel(column_names_trackers,10);
    private String column_names_smarms[]= {"Id","Tamaño en bytes","Pares conectados"};
    private DefaultTableModel table_model_smarms=new DefaultTableModel(column_names_smarms,10);

    public TrackerMain() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setSize(700, 500);
        setMinimumSize(new Dimension(700, 400));

        trackersPanel.add(tableTrackers.getTableHeader(), BorderLayout.NORTH);
        trackersPanel.add(tableTrackers, BorderLayout.CENTER);
        tableSmarms = new JTable(table_model_smarms);
        tableTrackers.setDefaultRenderer(Object.class, new MyCellRenderer());

        smarmsPanel.add(tableSmarms.getTableHeader(), BorderLayout.NORTH);
        smarmsPanel.add(tableSmarms, BorderLayout.CENTER);
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
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                TrackerService.getInstance().disconnect();
                JMSManager.getInstance().close();
                HibernateUtil.removeDatabase();
            }
        });
        setContentPane(mainPanel);
    }

    public synchronized void actualizarInterfaz(ConcurrentHashMap<String, TrackerKeepAlive> valores){
        table_model_trackers=new DefaultTableModel(column_names_trackers, 0);
        for(Map.Entry<String, TrackerKeepAlive> activeTracker : valores.entrySet()) {
            String[] temp = {activeTracker.getValue().getId(), (activeTracker.getValue().isMaster())?"Si":"No", activeTracker.getValue().getLastKeepAlive().toString(), activeTracker.getValue().getConfirmacionActualizacion().toString() };
            table_model_trackers.addRow(temp);
        }
        tableTrackers.setModel(table_model_trackers);
        table_model_trackers.fireTableDataChanged();
    }
    @Override
    public void update(Observable o, Object arg) {
        actualizarInterfaz((ConcurrentHashMap<String, TrackerKeepAlive>) arg);
    }
    public class MyCellRenderer extends javax.swing.table.DefaultTableCellRenderer {

        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final java.awt.Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try {
                Object val = table.getValueAt(row, 0);
                String sval = val.toString();
                if (sval.equals(TrackerService.getInstance().getTracker().getId())) {
                    cellComponent.setForeground(Color.black);
                    cellComponent.setBackground(Color.gray);
                } else {
                    cellComponent.setBackground(Color.white);
                    cellComponent.setForeground(Color.black);
                }
            }catch(Exception ignored){
            }
            return cellComponent;
        }
    }

}
