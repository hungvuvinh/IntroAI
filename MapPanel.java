import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.awt.BasicStroke;
import java.io.FileReader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
class Node {
    int id;
    double x, y;

    Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node other = (Node) obj;
            return this.id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

interface NodeSelectionListener {
    void onNodeSelected(Node start, Node end);
}

public class MapPanel extends JPanel {
    private final List<Node> nodes = new ArrayList<>();
    private final Map<Integer, List<Pair<Integer, Integer>>> adjacencyList = new HashMap<>();
    private Node startNode = null;
    private Node endNode = null;
    private List<Node> path = new ArrayList<>();
    private NodeSelectionListener listener;
    private static final int NODE_RADIUS = 5;
    private Set<Set<Integer>> blockedEdges = new HashSet<>();

    public MapPanel() {
        loadGraphFromJson();

        MouseInputAdapter mouseAdapter = new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double mouseX = e.getX();
                double mouseY = e.getY();

                for (Node node : nodes) {
                    double distance = Math.sqrt(Math.pow(mouseX - node.x, 2) + Math.pow(mouseY - node.y, 2));
                    if (distance <= NODE_RADIUS) {
                        if (startNode == null) {
                            startNode = node;
                        } else if (endNode == null) {
                            endNode = node;
                            path = findPathAStar(startNode, endNode);
                        } else {
                            startNode = node;
                            endNode = null;
                            path.clear();
                        }
                        if (listener != null) {
                            listener.onNodeSelected(startNode, endNode);
                        }
                        repaint();
                        break;
                    }
                }
            }
        };
        addMouseListener(mouseAdapter);
    }

    public void setNodeSelectionListener(NodeSelectionListener listener) {
        this.listener = listener;
    }

    public void resetSelection() {
        startNode = null;
        endNode = null;
        path.clear();
        repaint();
        if (listener != null) {
            listener.onNodeSelected(null, null);
        }
    }

    private void loadGraphFromJson() {
        try (FileReader reader = new FileReader("graph.json")) {
            JSONObject graph = new JSONObject(new org.json.JSONTokener(reader));
            JSONArray jsonNodes = graph.getJSONArray("nodes");
            JSONArray jsonEdges = graph.getJSONArray("edges");

            for (int i = 0; i < jsonNodes.length(); i++) {
                JSONObject jsonNode = jsonNodes.getJSONObject(i);
                nodes.add(new Node(jsonNode.getInt("id"), jsonNode.getDouble("x"), jsonNode.getDouble("y")));
            }

            for (int i = 0; i < jsonEdges.length(); i++) {
                JSONObject jsonEdge = jsonEdges.getJSONObject(i);
                int source = jsonEdge.getInt("source");
                int target = jsonEdge.getInt("target");
                int weight = (int) jsonEdge.getDouble("weight");

                adjacencyList.computeIfAbsent(source, k -> new ArrayList<>()).add(new Pair<>(target, weight));
                adjacencyList.computeIfAbsent(target, k -> new ArrayList<>( )).add(new Pair<>(source, weight));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Node> findPathAStar(Node start, Node end) {
        PriorityQueue<int[]> queue = new PriorityQueue<>(Comparator.comparingDouble(a -> a[2]));
        Map<Integer, Integer> cameFrom = new HashMap<>();
        Map<Integer, Double> gScore = new HashMap<>();
        queue.add(new int[]{start.id, -1, 0});
        gScore.put(start.id, 0.0);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currentId = current[0];

            if (currentId == end.id) {
                return reconstructPath(cameFrom, end);
            }

            Node currentNode = nodes.stream().filter(n -> n.id == currentId).findFirst().orElse(null);
            if (currentNode != null) {
                for (Pair<Integer, Integer> neighborPair : adjacencyList.getOrDefault(currentId, Collections.emptyList())) {
                    int neighborId = neighborPair.first;
                    int edgeWeight = neighborPair.second;

                    Set<Integer> edge = new HashSet<>(Arrays.asList(currentId, neighborId));
                    if (blockedEdges.contains(edge)) {
                        continue;
                    }

                    double tentativeGScore = gScore.getOrDefault(currentId, Double.MAX_VALUE) + edgeWeight;
                    if (tentativeGScore < gScore.getOrDefault(neighborId, Double.MAX_VALUE)) {
                        cameFrom.put(neighborId, currentId);
                        gScore.put(neighborId, tentativeGScore);
                        double priority = tentativeGScore + heuristic(neighborId, end.id);
                        queue.add(new int[]{neighborId, currentId, (int) priority});
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private List<Node> reconstructPath(Map<Integer, Integer> cameFrom, Node end) {
        List<Node> path = new ArrayList<>();
        Integer currentId = end.id;
        while (currentId != null) {
            final int nodeId = currentId;
            nodes.stream()
                    .filter(n -> n.id == nodeId)
                    .findFirst()
                    .ifPresent(path::add);
            currentId = cameFrom.get(currentId);
        }
        Collections.reverse(path);
        return path;
    }

    private double heuristic(int nodeId, int endId) {
        Node n1 = nodes.stream().filter(n -> n.id == nodeId).findFirst().get();
        Node n2 = nodes.stream().filter(n -> n.id == endId).findFirst().get();
        return Math.hypot(n1.x - n2.x, n1.y - n2.y);
    }

    public double getPathCost() {
        return path.isEmpty() ? 0.0 : path.size();
    }

    public void blockEdge(int u, int v) {
        Set<Integer> edge = new HashSet<>(Arrays.asList(u, v));
        blockedEdges.add(edge);
        repaint();
    }

    public void unblockEdge(int u, int v) {
        Set<Integer> edge = new HashSet<>(Arrays.asList(u, v));
        blockedEdges.remove(edge);
        repaint();
    }

    public void adjustEdgeWeight(int u, int v, int newWeight) {
        for (List<Pair<Integer, Integer>> neighbors : adjacencyList.values()) {
            for (Pair<Integer, Integer> neighbor : neighbors) {
                if ((neighbor.first == v && adjacencyList.containsKey(u) && adjacencyList.get(u).stream().anyMatch(p -> p.first == v)) ||
                    (neighbor.first == u && adjacencyList.containsKey(v) && adjacencyList.get(v).stream().anyMatch(p -> p.first == u))) {
                    neighbor.second = newWeight;
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        // Vẽ các cạnh
        for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : adjacencyList.entrySet()) {
            int sourceId = entry.getKey();
            Node sourceNode = nodes.stream().filter(n -> n.id == sourceId).findFirst().orElse(null);
            if (sourceNode != null) {
                for (Pair<Integer, Integer> targetPair : entry.getValue()) {
                    int targetId = targetPair.first;
                    Node targetNode = nodes.stream().filter(n -> n.id == targetId).findFirst().orElse(null);
                    if (targetNode != null) {
                        Set<Integer> edge = new HashSet<>(Arrays.asList(sourceId, targetId));
                        if (blockedEdges.contains(edge)) {
                            g2d.setColor(Color.RED);
                            g2d.setStroke(new BasicStroke(4));
                        } else {
                            g2d.setColor(Color.BLACK);
                            g2d.setStroke(new BasicStroke(2));
                        }
                        g2d.drawLine((int) sourceNode.x, (int) sourceNode.y, (int) targetNode.x, (int) targetNode.y);
                    }
                }
            }
        }

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        // Vẽ các node
        for (Node node : nodes) {
            if (node.equals(startNode)) {
                g2d.setColor(Color.GREEN);
            } else if (node.equals(endNode)) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLUE);
            }
            g2d.fillOval((int) node.x - NODE_RADIUS, (int) node.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        }

        // Vẽ đường đi (nếu có)
        if (!path.isEmpty()) {
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(3));
            for (int i = 0; i < path.size() - 1; i++) {
                Node n1 = path.get(i);
                Node n2 = path.get(i + 1);
                g2d.drawLine((int) n1.x, (int) n1.y, (int) n2.x, (int) n2.y);
            }
        }
    }

    private static class Pair<A, B> {
        A first;
        B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}