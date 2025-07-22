package main;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MoveTrackerPanel extends JPanel {
    private final JTextArea moveLogArea;
    private final List<String> whiteMoves = new ArrayList<>();
    private final List<String> blackMoves = new ArrayList<>();

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
        moveLogArea.setCaretColor(moveLogArea.getBackground());

        JScrollPane scrollPane = new JScrollPane(moveLogArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(92, 80, 61)));

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
        builder.append(String.format("%-9s | %-9s%n", "White", "Black"));
        builder.append("-----------------------\n");

        for (int i = 0; i < Math.max(whiteMoves.size(), blackMoves.size()); i++) {
            String whiteMove = i < whiteMoves.size() ? whiteMoves.get(i) : "";
            String blackMove = i < blackMoves.size() ? blackMoves.get(i) : "";
            builder.append(String.format("%d. %-6s | %-6s%n", i + 1, whiteMove, blackMove));        }

        moveLogArea.setText(builder.toString());
    }
}