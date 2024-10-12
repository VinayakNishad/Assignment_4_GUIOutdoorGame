import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class HelicopterStrike extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image helicopter1Img;
    Image helicopter2Img;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird positions and sizes
    int helicopter1X = boardWidth / 3;
    int helicopter1Y = boardHeight / 2;
    int helicopter2X = boardWidth / 8;
    int helicopter2Y = boardHeight / 3;
    int helicopterWidth = 60;
    int helicopterHeight = 50;

    class Helicopter {
        int x;
        int y;
        int width;
        int height;
        Image img;

        Helicopter(int x, int y, Image img) {
            this.x = x;
            this.y = y;
            this.width = helicopterWidth;
            this.height = helicopterHeight;
            this.img = img;
        }
    }

    // Pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // Scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean passed = false;

        Pipe(Image img, int x, int y) {
            this.img = img;
            this.x = x;
            this.y = y;
            this.width = pipeWidth;
            this.height = pipeHeight;
        }
    }

    // Game logic
    Helicopter helicopter1;
    Helicopter helicopter2;
    int velocityX = -4; // Move pipes to the left speed
    int velocityY1 = 0; // Helicopter 1 vertical speed
    int velocityY2 = 0; // Helicopter 2 vertical speed
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double helicopter1Score = 0;
    double helicopter2Score = 0;
    boolean helicopter1Collided = false;
    boolean helicopter2Collided = false;

    JButton startButton;

    HelicopterStrike() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
        setLayout(new BorderLayout()); // Use BorderLayout to position button

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("Images/background.png")).getImage();
        helicopter1Img = new ImageIcon(getClass().getResource("Images/helicoptor.png")).getImage();
        helicopter2Img = new ImageIcon(getClass().getResource("Images/player2.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("Images/obstacles.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("Images/obstacles.png")).getImage();

        // Initialize birds
        helicopter1 = new Helicopter(helicopter1X, helicopter1Y, helicopter1Img);
        helicopter2 = new Helicopter(helicopter2X, helicopter2Y, helicopter2Img);
        pipes = new ArrayList<>();

        // Setup start button
        startButton = new JButton("Start Game");
        startButton.addActionListener(e -> startGame());
        add(startButton, BorderLayout.SOUTH); // Add button to the bottom of the panel

        // Place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // Game timer
        gameLoop = new Timer(1000 / 40, this); // 60 FPS
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg, pipeX, randomPipeY);
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg, pipeX, topPipe.y + pipeHeight + openingSpace);
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, this);

        // Birds
        g.drawImage(helicopter1.img, helicopter1.x, helicopter1.y, helicopter1.width, helicopter1.height, this);
        g.drawImage(helicopter2.img, helicopter2.x, helicopter2.y, helicopter2.width, helicopter2.height, this);

        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, this);
        }

        // Score display
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        if (gameOver) {
            g.drawString("Game Over", 125, 35);
            g.drawString("Red Score: " + (int) helicopter1Score, 10, 55);
            g.drawString("Blue Score: " + (int) helicopter2Score, boardWidth - 150, 55);
        } else {
            g.drawString("Red Score: " + (int) helicopter1Score, 10, 35);
            g.drawString("Blue Score: " + (int) helicopter2Score, boardWidth - 150, 35);
        }
    }

    public void move() {
        // Update velocities for gravity
        velocityY1 += gravity;
        velocityY2 += gravity;

        // Move helicopter
        helicopter1.y += velocityY1;
        helicopter1.y = Math.max(helicopter1.y, 0); // Ensure stays within bounds

        helicopter2.y += velocityY2;
        helicopter2.y = Math.max(helicopter2.y, 0); // Ensure stays within bounds

        // Move pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && helicopter1.x > pipe.x + pipe.width) {
                helicopter1Score += 0.5; // Increment score for helicopter 1
                pipe.passed = true;
            }

            if (!pipe.passed && helicopter2.x > pipe.x + pipe.width) {
                helicopter2Score += 0.5; // Increment score for helicopter 2
                pipe.passed = true;
            }

            if (collision(helicopter1, pipe)) {
                helicopter1Collided = true;
            }

            if (collision(helicopter2, pipe)) {
                helicopter2Collided = true;
            }
        }

        if (helicopter1.y > boardHeight) {
            helicopter1Collided = true;
        }
        
        if (helicopter2.y > boardHeight) {
            helicopter2Collided = true;
        }

        // Game over if both are collided
        if (helicopter1Collided && helicopter2Collided) {
            gameOver = true;
            showGameOverDialog();
        }
    }

    boolean collision(Helicopter helicopter, Pipe pipe) {
        return helicopter.x < pipe.x + pipe.width &&
               helicopter.x + helicopter.width > pipe.x &&
               helicopter.y < pipe.y + pipe.height &&
               helicopter.y + helicopter.height > pipe.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    private void startGame() {
        helicopter1.y = helicopter1Y;
        helicopter2.y = helicopter2Y; // Reset second helicopter position
        velocityY1 = 0;
        velocityY2 = 0;
        pipes.clear();
        gameOver = false;
        helicopter1Collided = false;
        helicopter2Collided = false;
        helicopter1Score = 0;
        helicopter2Score = 0;

        startButton.setEnabled(false); // Disable the start button

        placePipeTimer.start();
        gameLoop.start();
    }

    private void showGameOverDialog() {
        String message = "Blue Score: " + (int) helicopter1Score + "\n" +
                         "Red Score: " + (int) helicopter2Score;
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        startButton.setEnabled(true); // Re-enable the start button
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // Control for helicopter 1
            velocityY1 = -9;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) {
            // Control for helicopter 2
            velocityY2 = -9;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
