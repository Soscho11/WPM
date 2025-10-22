import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SpeedyKeys extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JComboBox<String> modeBox;
    private JTextArea sentenceArea;
    private JTextArea typingArea;
    private JLabel timerLabel;
    private Timer timer;
    private int timeLeft = 60;
    private long startTime;
    private boolean typingStarted = false;
    private String currentSentence;
    private boolean darkMode = true;
    private JButton submitBtn;

    // 🎨 Dark Theme
    private final Color DARK_BG = new Color(25, 25, 35);
    private final Color DARK_PANEL = new Color(35, 35, 50);
    private final Color DARK_TEXT = new Color(230, 230, 240);
    private final Color DARK_ACCENT = new Color(97, 179, 255);

    // ☀️ Light Theme
    private final Color LIGHT_BG = new Color(245, 245, 250);
    private final Color LIGHT_PANEL = new Color(255, 255, 255);
    private final Color LIGHT_TEXT = new Color(25, 25, 35);
    private final Color LIGHT_ACCENT = new Color(50, 120, 255);

    public SpeedyKeys() {
        setTitle("SpeedyKeys - Typing Test");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createHomePanel(), "HOME");
        mainPanel.add(createTypingPanel(), "TYPING");

        add(mainPanel);
        applyTheme(); // initialize dark mode colors
        setVisible(true);
    }

    // 🏠 Home Screen
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("HOME_PANEL");

        JLabel title = new JLabel("SpeedyKeys Typing Test", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setName("TITLE_LABEL");

        String[] modes = {"Easy", "Medium", "Hard"};
        modeBox = new JComboBox<>(modes);
        styleComboBox(modeBox);

        JButton startBtn = new JButton("Start Test");
        styleButton(startBtn);

        JButton themeBtn = new JButton("🌙 Dark Mode");
        styleButton(themeBtn);
        themeBtn.addActionListener(e -> {
            darkMode = !darkMode;
            themeBtn.setText(darkMode ? "🌙 Dark Mode" : "☀️ Light Mode");
            applyTheme();
        });

        startBtn.addActionListener(e -> {
            String mode = (String) modeBox.getSelectedItem();
            loadSentence(mode);
            cardLayout.show(mainPanel, "TYPING");
        });

        JPanel centerPanel = new JPanel();
        centerPanel.setName("CENTER_PANEL");
        centerPanel.add(new JLabel("Select Mode: ") {{
            setFont(new Font("Segoe UI", Font.PLAIN, 16));
            setName("LABEL_MODE");
        }});
        centerPanel.add(modeBox);
        centerPanel.add(startBtn);
        centerPanel.add(themeBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    // ⌨️ Typing Screen
    private JPanel createTypingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setName("TYPING_PANEL");

        sentenceArea = new JTextArea();
        sentenceArea.setEditable(false);
        sentenceArea.setLineWrap(true);
        sentenceArea.setWrapStyleWord(true);
        sentenceArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        sentenceArea.setName("SENTENCE_AREA");

        typingArea = new JTextArea();
        typingArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        typingArea.setLineWrap(true);
        typingArea.setWrapStyleWord(true);
        typingArea.setName("TYPING_AREA");

        timerLabel = new JLabel("Time: 60s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setName("TIMER_LABEL");

        submitBtn = new JButton("Submit");
        styleButton(submitBtn);
        submitBtn.setEnabled(false); // 🔒 Initially disabled
        submitBtn.addActionListener(e -> calculateResult());

        typingArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!typingStarted) {
                    startTimer();
                    typingStarted = true;
                }
            }
        });

        // ✅ Enable Submit only if typed text matches beginning of sentence
        typingArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkText(); }
            public void removeUpdate(DocumentEvent e) { checkText(); }
            public void changedUpdate(DocumentEvent e) { checkText(); }

            private void checkText() {
                String typed = typingArea.getText();
                // Enable submit only if typed text is non-empty AND matches beginning of sentence
                if (!typed.trim().isEmpty() && currentSentence.startsWith(typed)) {
                    submitBtn.setEnabled(true);
                } else {
                    submitBtn.setEnabled(false);
                }
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setName("BOTTOM_PANEL");
        bottomPanel.add(timerLabel);
        bottomPanel.add(submitBtn);

        panel.add(new JScrollPane(sentenceArea), BorderLayout.NORTH);
        panel.add(new JScrollPane(typingArea), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // 🎨 Button Styling
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // 🎨 ComboBox Styling
    private void styleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setFocusable(false);
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
    }

    // 🌗 Apply Theme
    private void applyTheme() {
        Color bg = darkMode ? DARK_BG : LIGHT_BG;
        Color panel = darkMode ? DARK_PANEL : LIGHT_PANEL;
        Color text = darkMode ? DARK_TEXT : LIGHT_TEXT;
        Color accent = darkMode ? DARK_ACCENT : LIGHT_ACCENT;

        getContentPane().setBackground(bg);
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel) {
                updatePanelColors((JPanel) comp, bg, panel, text, accent);
            }
        }
        repaint();
    }

    private void updatePanelColors(JPanel panel, Color bg, Color inner, Color text, Color accent) {
        panel.setBackground(inner);
        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel label) {
                label.setForeground(text);
            } else if (c instanceof JPanel subPanel) {
                updatePanelColors(subPanel, bg, inner, text, accent);
            } else if (c instanceof JTextArea area) {
                area.setBackground(bg);
                area.setForeground(text);
                area.setCaretColor(accent);
                area.setBorder(BorderFactory.createLineBorder(accent, 1, true));
            } else if (c instanceof JButton button) {
                button.setBackground(accent);
                button.setForeground(Color.WHITE);
            } else if (c instanceof JComboBox<?> combo) {
                combo.setBackground(bg);
                combo.setForeground(text);
            }
        }
    }

    // 🕒 Timer Logic
    private void startTimer() {
        startTime = System.currentTimeMillis();
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft + "s");
            if (timeLeft <= 0) {
                timer.stop();
                calculateResult();
            }
        });
        timer.start();
    }

    // 🧾 Sentence Loader
    private void loadSentence(String mode) {
        String[] easy = {
                "The cat jumps over the wall.",
                "Typing is fun when you practice.",
                "I love to learn new things every day."
        };
        String[] medium = {
                "Java Swing makes creating GUIs simple and interactive.",
                "Practice improves accuracy and speed over time.",
                "Consistency is the key to mastering any skill."
        };
        String[] hard = {
                "Programming challenges our logic, patience, and creativity.",
                "Developers often debug their code line by line.",
                "Artificial intelligence is shaping the future of technology."
        };

        Random r = new Random();
        if (mode.equals("Easy"))
            currentSentence = easy[r.nextInt(easy.length)];
        else if (mode.equals("Medium"))
            currentSentence = medium[r.nextInt(medium.length)];
        else
            currentSentence = hard[r.nextInt(hard.length)];

        sentenceArea.setText(currentSentence);
        typingArea.setText("");
        timeLeft = 60;
        timerLabel.setText("Time: 60s");
        typingStarted = false;
        submitBtn.setEnabled(false); // 🔒 Reset submit button
    }

    // 🧮 Calculate WPM and Accuracy
    private void calculateResult() {
        if (timer != null) timer.stop();

        String typedText = typingArea.getText();
        if (typedText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "You didn't type anything!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double minutes = (System.currentTimeMillis() - startTime) / 60000.0;
        int wordsTyped = typedText.split("\\s+").length;
        int correctWords = 0;

        String[] origWords = currentSentence.split("\\s+");
        String[] typedWords = typedText.split("\\s+");

        for (int i = 0; i < Math.min(origWords.length, typedWords.length); i++) {
            if (origWords[i].equals(typedWords[i])) correctWords++;
        }

        double accuracy = ((double) correctWords / origWords.length) * 100;
        int wpm = (int) (wordsTyped / minutes);

        JPanel resultPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        resultPanel.setBackground(darkMode ? DARK_BG : LIGHT_BG);
        resultPanel.add(createStyledLabel("⏱ Time: " + (60 - timeLeft) + "s"));
        resultPanel.add(createStyledLabel("💨 WPM: " + wpm));
        resultPanel.add(createStyledLabel("🎯 Accuracy: " + String.format("%.2f", accuracy) + "%"));

        JOptionPane.showMessageDialog(this, resultPanel, "Test Result", JOptionPane.PLAIN_MESSAGE);
        cardLayout.show(mainPanel, "HOME");
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(darkMode ? DARK_TEXT : LIGHT_TEXT);
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpeedyKeys::new);
    }
}
