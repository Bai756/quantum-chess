package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatMain extends JPanel{
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    private void sendMessage()
    {
        String trashtalk =  inputField.getText().trim();
        if (!trashtalk.isEmpty())
        {
            chatArea.append("This guy said: " +  trashtalk + "\n");
            inputField.setText("");
        }
    }
    public void automsg(String msg)
    {
        chatArea.append(msg);
    }
    public ChatMain() {
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
    }
}
