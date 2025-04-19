import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.Image;

public class MemoryCardGame extends JFrame {
    private static final int ROWS = 4;
    private static final int COLS = 4;
    private JButton[][] cards;
    private String[][] cardValues;
    private boolean[][] flipped;
    private int flippedCount;
    private int firstRow, firstCol;
    private Timer matchTimer;
    private Timer gameTimer;
    private int elapsedSeconds;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private int score;
    private int matchesFound;
    private boolean isPaused;
    private String currentTheme;  
    
    public MemoryCardGame(String theme) {
        currentTheme = theme;
        cards = new JButton[ROWS][COLS];
        cardValues = new String[ROWS][COLS];
        flipped = new boolean[ROWS][COLS];
        flippedCount = 0;
        elapsedSeconds = 0;
        score = 0;
        matchesFound = 0;
        isPaused = false;
        setTitle("Memory Card Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 240, 240));
        createTopPanel();
        createCardGrid();
        createBottomPanel();
        loadCards(theme);
        startGameTimer();
        pack();
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(220, 220, 220));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        timerLabel = new JLabel("Time: 0 seconds");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(timerLabel);
        scoreLabel = new JLabel("   Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(scoreLabel);
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void createCardGrid() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(ROWS, COLS, 10, 10));
        gridPanel.setBackground(new Color(200, 200, 200));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int row = i;
                final int col = j;
                cards[i][j] = new JButton();
                cards[i][j].setPreferredSize(new Dimension(100, 100));
                cards[i][j].setBackground(new Color(70, 130, 180));
                cards[i][j].setBorder(BorderFactory.createRaisedBevelBorder());
                cards[i][j].setText("?");
                cards[i][j].setFont(new Font("Arial", Font.BOLD, 24));
                cards[i][j].setForeground(Color.WHITE);
                cards[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (!isPaused && !flipped[row][col] && flippedCount < 2) {
                            flipCard(row, col);
                        }
                    }
                });
                gridPanel.add(cards[i][j]);
                flipped[i][j] = false;
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(220, 220, 220));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });
        final JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                togglePause(pauseButton);
            }
        });
        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseTheme();
            }
        });
        bottomPanel.add(restartButton);
        bottomPanel.add(pauseButton);
        bottomPanel.add(newGameButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadCards(String theme) {
        ArrayList<String> values = new ArrayList<String>();
        currentTheme = theme;
        if (theme.equalsIgnoreCase("Animals")) {
            values.add("dog.jpg");
            values.add("dog2.jpg");
            values.add("cat.jpg");
            values.add("cat2.jpg");
            values.add("tiger.jpg");
            values.add("tiger2.jpg");
            values.add("wolf.jpg");
            values.add("wolf2.jpg");
            values.add("bear.jpg");
            values.add("bear2.jpg");
            values.add("bird.jpg");
            values.add("bird2.jpg");
            values.add("rabbit.jpg");
            values.add("rabbit2.jpg");
            values.add("monkey.jpg");
            values.add("monkey2.jpg");
        } else {
            for (int i = 1; i <= (ROWS * COLS / 2); i++) {
                values.add(i + ".jpg");
                values.add(i + i + ".jpg");
            }
        }
        Collections.shuffle(values);
        int index = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cardValues[i][j] = values.get(index++);
            }
        }
    }
    
    private void flipCard(int row, int col) {
        flipped[row][col] = true;
        flippedCount++;
        cards[row][col].setText("");
        ImageIcon originalIcon = new ImageIcon("images/" + cardValues[row][col]);
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        cards[row][col].setIcon(scaledIcon);
        cards[row][col].setBackground(new Color(60, 179, 113));
        if (flippedCount == 1) {
            firstRow = row;
            firstCol = col;
        } else if (flippedCount == 2) {
            checkMatch(row, col);
        }
    }
    
    private boolean isMatchingPair(String card1, String card2) {
        if (currentTheme.equalsIgnoreCase("Animals")) {
            String base1 = card1.replace("2.jpg", ".jpg");
            String base2 = card2.replace("2.jpg", ".jpg");
            return base1.equals(base2);
        } else {
            String num1 = card1.replace(".jpg", "").replace("11", "1").replace("22", "2")
                               .replace("33", "3").replace("44", "4").replace("55", "5")
                               .replace("66", "6").replace("77", "7").replace("88", "8")
                               .replace("99", "9");
            String num2 = card2.replace(".jpg", "").replace("11", "1").replace("22", "2")
                               .replace("33", "3").replace("44", "4").replace("55", "5")
                               .replace("66", "6").replace("77", "7").replace("88", "8")
                               .replace("99", "9");
            return num1.equals(num2);
        }
    }
    
    private void checkMatch(final int row, final int col) {
        final boolean isMatch = isMatchingPair(cardValues[firstRow][firstCol], cardValues[row][col]);
        matchTimer = new Timer(800, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isMatch) {
                    cards[firstRow][firstCol].setEnabled(false);
                    cards[firstRow][firstCol].setBackground(new Color(34, 139, 34));
                    cards[row][col].setEnabled(false);
                    cards[row][col].setBackground(new Color(34, 139, 34));
                    score += 10;
                    matchesFound++;
                    scoreLabel.setText("   Score: " + score);
                    if (matchesFound == (ROWS * COLS / 2)) {
                        gameOver();
                    }
                } else {
                    cards[firstRow][firstCol].setText("?");
                    cards[firstRow][firstCol].setIcon(null);
                    cards[firstRow][firstCol].setBackground(new Color(70, 130, 180));
                    flipped[firstRow][firstCol] = false;
                    cards[row][col].setText("?");
                    cards[row][col].setIcon(null);
                    cards[row][col].setBackground(new Color(70, 130, 180));
                    flipped[row][col] = false;
                    score = Math.max(0, score - 2);
                    scoreLabel.setText("   Score: " + score);
                }
                flippedCount = 0;
                matchTimer.stop();
            }
        });
        matchTimer.setRepeats(false);
        matchTimer.start();
    }
    
    private void startGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                elapsedSeconds++;
                timerLabel.setText("Time: " + elapsedSeconds + " seconds");
            }
        });
        gameTimer.start();
    }
    
    private void togglePause(final JButton pauseButton) {
        isPaused = !isPaused;
        if (isPaused) {
            gameTimer.stop();
            pauseButton.setText("Resume");
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (flipped[i][j] && cards[i][j].isEnabled()) {
                        cards[i][j].setIcon(null);
                        cards[i][j].setText("PAUSED");
                    }
                }
            }
        } else {
            gameTimer.start();
            pauseButton.setText("Pause");
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (flipped[i][j] && cards[i][j].isEnabled()) {
                        cards[i][j].setText("");
                        ImageIcon originalIcon = new ImageIcon("images/" + cardValues[i][j]);
                        Image originalImage = originalIcon.getImage();
                        Image scaledImage = originalImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(scaledImage);
                        cards[i][j].setIcon(scaledIcon);
                    }
                }
            }
        }
    }
    
    private void restartGame() {
        flippedCount = 0;
        elapsedSeconds = 0;
        score = 0;
        matchesFound = 0;
        isPaused = false;
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
        gameTimer.start();
        timerLabel.setText("Time: 0 seconds");
        scoreLabel.setText("   Score: 0");
        loadCards(currentTheme);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                flipped[i][j] = false;
                cards[i][j].setIcon(null);
                cards[i][j].setText("?");
                cards[i][j].setBackground(new Color(70, 130, 180));
                cards[i][j].setEnabled(true);
            }
        }
    }
    
    private void gameOver() {
        gameTimer.stop();
        int timeBonus = Math.max(0, 100 - elapsedSeconds);
        int finalScore = score + timeBonus;
        String message = "Congratulations! You won!\n\n" +
                         "Matches found: " + matchesFound + "\n" +
                         "Time taken: " + elapsedSeconds + " seconds\n" +
                         "Score: " + score + "\n" +
                         "Time bonus: " + timeBonus + "\n" +
                         "Final score: " + finalScore;
        JOptionPane.showMessageDialog(this, message, "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void chooseTheme() {
        String[] options = {"Animals", "Numbers"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Choose a card theme:",
            "New Game",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        if (choice >= 0) {
            loadCards(options[choice]);
            restartGame();
        }
    }
    
    public static void main(String[] args) {
        String[] themes = {"Animals", "Numbers"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Choose a card theme to start:",
            "Memory Card Game",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            themes,
            themes[0]
        );
        if (choice >= 0) {
            new MemoryCardGame(themes[choice]);
        } else {
            System.exit(0);
        }
    }
} 