// User.java
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class User {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Viewer with A*");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            MapPanel mapPanel = new MapPanel();
            JPanel controlPanel = new JPanel();

            JLabel startLabel = new JLabel("Start Node: None");
            JLabel endLabel = new JLabel("End Node: None");
            JLabel pathLabel = new JLabel("Path Cost: 0");
            JButton resetButton = new JButton("Reset");

            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mapPanel.resetSelection();
                    startLabel.setText("Start Node: None");
                    endLabel.setText("End Node: None");
                    pathLabel.setText("Path Cost: 0");
                }
            });

            controlPanel.add(startLabel);
            controlPanel.add(endLabel);
            controlPanel.add(pathLabel);
            controlPanel.add(resetButton);

            mapPanel.setNodeSelectionListener((start, end) -> {
                startLabel.setText("Start Node: " + (start != null ? start.id : "None"));
                endLabel.setText("End Node: " + (end != null ? end.id : "None"));

                if (start != null && end != null) {
                    Container pathCost = mapPanel.getParent();
                    pathLabel.setText("Path Cost: " + pathCost);
                } else {
                    pathLabel.setText("Path Cost: 0");
                }
            });

            frame.add(mapPanel, BorderLayout.CENTER);
            frame.add(controlPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
        });
    }
}

