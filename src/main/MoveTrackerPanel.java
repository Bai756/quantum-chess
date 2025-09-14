package main;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class MoveTrackerPanel extends JPanel {
    private final JTextArea moveLogArea;
    private final JLabel headerLabel;
    private final JLabel dashLabel;
    private final java.util.List<String> whiteMoves = new ArrayList<>();
    private final java.util.List<String> blackMoves = new ArrayList<>();

    public MoveTrackerPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 400));
        setBackground(new Color(117, 110, 93));

        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(57, 57, 57));

        headerLabel = new JLabel(String.format("%-16s | %-16s", "White", "Black"));
        headerLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        dashLabel = new JLabel("-----------------------------------");
        dashLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        dashLabel.setForeground(Color.WHITE);
        dashLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        headerPanel.add(headerLabel);
        headerPanel.add(dashLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Move log area
        moveLogArea = new JTextArea();
        moveLogArea.setEditable(false);
        moveLogArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        moveLogArea.setBackground(new Color(57, 57, 57));
        moveLogArea.setForeground(Color.WHITE);
        moveLogArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        moveLogArea.setCaretColor(moveLogArea.getBackground());

        JScrollPane scrollPane = new JScrollPane(moveLogArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(57, 57, 57)));

        add(scrollPane, BorderLayout.CENTER);

        updateDisplay();
    }

    public void logMove(String moveText) {
        if (moveText.startsWith("White: ")) {
            whiteMoves.add(moveText.substring(7));
        } else if (moveText.startsWith("Black: ")) {
            blackMoves.add(moveText.substring(7));
        }
        updateDisplay();
    }

    private void updateDisplay() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.max(whiteMoves.size(), blackMoves.size()); i++) {
            String whiteMove = i < whiteMoves.size() ? whiteMoves.get(i) : "";
            String blackMove = i < blackMoves.size() ? blackMoves.get(i) : "";
            builder.append(String.format("%2d. %-12s | %-12s%n", i + 1, whiteMove, blackMove));
        }
        moveLogArea.setText(builder.toString());
    }
}