package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatMain extends JPanel{
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private int currentColor; // 0 = WHITE, 1 = BLACK


    public ChatMain() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(300, 400));
        setBackground(new Color(117, 110, 93)); // This is first part.

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.BOLD, 14));
        chatArea.setForeground(new Color(0,0,0));
        chatArea.setBackground(new Color(105, 73, 55)); // parchment-style tone
        chatArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(92, 80, 61), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 130, 100), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(new Color(125, 100, 80));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createLineBorder(new Color(100, 80, 60)));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        sendButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(new Color(145, 115, 95));
            }

            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(new Color(125, 100, 80));
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBackground(new Color(117, 110, 93)); // This is second part.
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }



    public void setCurrentColor(int color) {
        this.currentColor = color;
    }

    private void sendMessage() {
        String trashtalk = inputField.getText().trim();
        if (!trashtalk.isEmpty()) {
            String playerLabel = (currentColor == 0) ? "White" : "Black";
            chatArea.append(playerLabel + ": " + trashtalk + "\n");
            inputField.setText("");
        }
    }
public void displaySystemMessage(String msg) {
        chatArea.append("[System] " + msg + "\n");
    }
    public void automsg(String msg)
    {
        chatArea.append(msg);
    }
    /*public ChatMain() {
        setLayout( new BorderLayout());
        setPreferredSize(new Dimension(300, 400));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatArea.setFont(new Font("SanSerif", Font.BOLD, 14));
        chatArea.setForeground(new Color(30, 30, 30));
        chatArea.setBackground(new Color(180, 165, 125));

        inputField = new JTextField();
        sendButton = new JButton("u sure u wanna send bro?");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        //sendButton.addActionListener(new ActionListener(e -> sendMessage()) );
        //inputField.addActionListener(new ActionListener(e -> sendMessage()) );
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }*/
}
