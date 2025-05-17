import javax.swing.*;
import java.awt.*;
public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo MapPanel (dùng chung cho cả User và Admin)
            MapPanel mapPanel = new MapPanel();

            // Khởi tạo User Interface (Giao diện người dùng)
            JFrame userFrame = new JFrame("Graph Viewer with A*");
            userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            userFrame.setSize(800, 600);

            JPanel controlPanel = new JPanel();
            JLabel startLabel = new JLabel("Start Node: None");
            JLabel endLabel = new JLabel("End Node: None");
            JLabel pathLabel = new JLabel("Path Cost: 0");
            JButton resetButton = new JButton("Reset");
            JButton adminButton = new JButton("Open Admin"); // Nút mở Admin

            resetButton.addActionListener(e -> {
                mapPanel.resetSelection();
                startLabel.setText("Start Node: None");
                endLabel.setText("End Node: None");
                pathLabel.setText("Path Cost: 0");
            });

            adminButton.addActionListener(e -> {
                // Mở AdminInterface và truyền mapPanel vào
                AdminInterface adminFrame = new AdminInterface(mapPanel);
                adminFrame.setVisible(true);
            });

            controlPanel.add(startLabel);
            controlPanel.add(endLabel);
            controlPanel.add(pathLabel);
            controlPanel.add(resetButton);
            controlPanel.add(adminButton); // Thêm nút Admin

            mapPanel.setNodeSelectionListener((start, end) -> {
                startLabel.setText("Start Node: " + (start != null ? start.id : "None"));
                endLabel.setText("End Node: " + (end != null ? end.id : "None"));

                if (start != null && end != null) {
                    pathLabel.setText("Path Cost: " + mapPanel.getPathCost()); // Giả sử có getPathCost()
                } else {
                    pathLabel.setText("Path Cost: 0");
                }
            });

            userFrame.add(mapPanel, BorderLayout.CENTER);
            userFrame.add(controlPanel, BorderLayout.SOUTH);
            userFrame.setVisible(true);
        });
    }
}