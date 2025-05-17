import javax.swing.*;
import java.awt.*;

public class AdminInterface extends JFrame {
    private JComboBox<String> blockTypeSelector;
    private JTextField nodeField, nodeUField, nodeVField, weightField;
    private JButton blockButton, unblockButton, adjustWeightButton;
    private MapPanel mapPanel; // Tham chiếu đến MapPanel

    public AdminInterface(MapPanel mapPanel) { // Constructor nhận MapPanel
        this.mapPanel = mapPanel;
        setTitle("Admin Interface");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Đóng cửa sổ Admin không tắt ứng dụng
        setSize(400, 350);
        setLayout(new FlowLayout());

        add(new JLabel("Block Type:"));
        blockTypeSelector = new JComboBox<>(new String[]{"edge", "node"});
        add(blockTypeSelector);

        nodeUField = new JTextField(10);
        nodeVField = new JTextField(10);
        weightField = new JTextField(5);
        nodeField = new JTextField(10);

        add(new JLabel("Node U:"));
        add(nodeUField);
        add(new JLabel("Node V:"));
        add(nodeVField);
        add(new JLabel("Weight:"));
        add(weightField);

        add(new JLabel("Node:"));
        add(nodeField);

        blockButton = new JButton("Block");
        unblockButton = new JButton("Unblock");
        adjustWeightButton = new JButton("Adjust Weight");

        add(blockButton);
        add(unblockButton);
        add(adjustWeightButton);

        blockTypeSelector.addActionListener(e -> updateFieldsVisibility());
        updateFieldsVisibility();

        blockButton.addActionListener(e -> handleBlock());
        unblockButton.addActionListener(e -> handleUnblock());
        adjustWeightButton.addActionListener(e -> handleAdjustWeight());
    }

    private void updateFieldsVisibility() {
        String type = (String) blockTypeSelector.getSelectedItem();
        boolean isEdge = "edge".equals(type);

        nodeUField.setVisible(isEdge);
        nodeVField.setVisible(isEdge);
        weightField.setVisible(isEdge);
        adjustWeightButton.setVisible(isEdge);
        nodeField.setVisible(!isEdge);

        revalidate();
        repaint();
    }

    private void handleBlock() {
        String type = (String) blockTypeSelector.getSelectedItem();
        if ("edge".equals(type)) {
            try {
                int u = Integer.parseInt(nodeUField.getText().trim());
                int v = Integer.parseInt(nodeVField.getText().trim());
                mapPanel.blockEdge(u, v); // Gọi phương thức của MapPanel
                System.out.println("Block edge: " + u + " - " + v);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid node IDs!");
            }
        } else {
            String node = nodeField.getText().trim();
            System.out.println("Block node: " + node);
            JOptionPane.showMessageDialog(this, "Node blocking is not supported in this version!");
        }
    }

    private void handleUnblock() {
        String type = (String) blockTypeSelector.getSelectedItem();
        if ("edge".equals(type)) {
            try {
                int u = Integer.parseInt(nodeUField.getText().trim());
                int v = Integer.parseInt(nodeVField.getText().trim());
                mapPanel.unblockEdge(u, v); // Gọi phương thức của MapPanel
                System.out.println("Unblock edge: " + u + " - " + v);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid node IDs!");
            }
        } else {
            String node = nodeField.getText().trim();
            System.out.println("Unblock node: " + node);
            JOptionPane.showMessageDialog(this, "Node unblocking is not supported in this version!");
        }
    }

    private void handleAdjustWeight() {
        try {
            int u = Integer.parseInt(nodeUField.getText().trim());
            int v = Integer.parseInt(nodeVField.getText().trim());
            int w = Integer.parseInt(weightField.getText().trim());
            mapPanel.adjustEdgeWeight(u, v, w); // Gọi phương thức của MapPanel
            System.out.println("Adjust weight of edge " + u + " - " + v + " to " + w);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input for node IDs or weight!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Tạo MapPanel
            MapPanel mapPanel = new MapPanel();

            // Tạo AdminInterface và truyền MapPanel vào
            AdminInterface frame = new AdminInterface(mapPanel);
            frame.setVisible(true);
        });
    }

    static class Edge {
        String u, v;
        int weight;

        public Edge(String u, String v, int weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }
    }
}