package main;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MoveTrackerPanel extends JPanel {
    private JTextArea moveLogArea;
    private List<String> whiteMoves = new ArrayList<>();
    private List<String> blackMoves = new ArrayList<>();
    private int turnCount = 1;

    public MoveTrackerPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 400));
        setBackground(new Color(117, 110, 93));

        moveLogArea = new JTextArea();
        moveLogArea.setEditable(false);
        moveLogArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        moveLogArea.setBackground(new Color(57, 57, 57));
        moveLogArea.setForeground(Color.WHITE);
        moveLogArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(moveLogArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(92, 80, 61)));

        add(scrollPane, BorderLayout.CENTER);
    }

    public void logMove(String moveText) {
        if (moveText.startsWith("White:")) {
            whiteMoves.add(moveText.substring(7)); // remove "White: "
        } else if (moveText.startsWith("Black: ")) {
            blackMoves.add(moveText.substring(7)); // remove "Black: "
        }

        updateDisplay();
    }

    private void updateDisplay() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-10s | %-8s%n", "White", "Black"));
        builder.append("-------------------------\n");

        for (int i = 0; i < Math.max(whiteMoves.size(), blackMoves.size()); i++) {
            String whiteMove = i < whiteMoves.size() ? whiteMoves.get(i) : "";
            String blackMove = i < blackMoves.size() ? blackMoves.get(i) : "";
            builder.append(String.format("%-2d. %-6s | %-6s%n", i + 1, whiteMove, blackMove));
        }

        moveLogArea.setText(builder.toString());
    }
}
