package main;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TutorialPanel extends JPanel {
    public int step = 0;
    private final JTextArea tutorialText;
    public final RoundedButton nextButton;
    private final MouseAdapter hoverEffect;
    private Consumer<Integer> onStepChange;
    public int maxStep = 8;


    public TutorialPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // Hover effect for button
        hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton source = (JButton) e.getSource();
                source.setBackground(new Color(70, 70, 70));
                source.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                JButton source = (JButton) e.getSource();
                source.setBackground(new Color(50, 50, 50, 200));
                source.setBorder(BorderFactory.createLineBorder(new Color(240,230,203), 3, true));
            }
        };

        JLabel title = new JLabel("Tutorial", SwingConstants.CENTER);
        title.setFont(new Font("Book Antiqua", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(240,230,203));
        add(title);
        add(Box.createVerticalStrut(10));

        tutorialText = new JTextArea();
        tutorialText.setEditable(false);
        tutorialText.setFont(new Font("SansSerif", Font.PLAIN, 18));
        tutorialText.setLineWrap(true);
        tutorialText.setWrapStyleWord(true);
        tutorialText.setOpaque(false);
        tutorialText.setAlignmentX(Component.CENTER_ALIGNMENT);
        tutorialText.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        tutorialText.setForeground(new Color(240,230,203));
        add(tutorialText);

        nextButton = new RoundedButton("Next");
        buttonFormat(nextButton);
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(_ -> advanceStep());
        add(Box.createVerticalStrut(15));
        add(nextButton);

        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setStep(0);
    }

    private void buttonFormat(RoundedButton button){
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setForeground(new Color(240,230,203));
        button.setBackground(new Color(50, 50, 50, 200));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(240, 230, 203), 2));
        button.setPreferredSize(new Dimension(200, 40));
//        button.addMouseListener(hoverEffect);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30);
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fill(round);
        g2.setColor(new Color(240,230,203, 180));
        g2.setStroke(new BasicStroke(3));
        g2.draw(round);
        g2.dispose();
        super.paintComponent(g);
    }

    public void setOnStepChange(java.util.function.Consumer<Integer> callback) {
        this.onStepChange = callback;
    }

    private void setStep(int s) {
        step = s;
        switch (step) {
            case 0 -> tutorialText.setText("Move your e pawn forward 2 squares.");
            case 1 -> tutorialText.setText("Try splitting your e pawn.");
            case 2 -> tutorialText.setText("Hold tab to see the probabilities. The number is the percentage chance the piece is there.");
            case 3 -> tutorialText.setText("(Pieces moved and added just for tutorial)\nIf you split a piece at least twice, you can amplify it. Amplify your e pawn.");
            case 4 -> tutorialText.setText("Try capturing a split piece and see what happens.");
            case 5 -> tutorialText.setText("If green, you've captured the piece. If orange, only the attacker or defender was there and you did not capture the piece. If red, both pieces were not there. Read the rules for more information.");
            case 6 -> tutorialText.setText("Capture the opponent's king to win.");
            default -> tutorialText.setText("Tutorial complete! Read the rules for more information.");
        }
        if (onStepChange != null) onStepChange.accept(step);
        revalidate();
        repaint();
    }

    public void advanceStep() {
        setStep(step + 1);
    }
}
