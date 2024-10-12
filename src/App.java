import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Helicopter Strike");
            
            HelicopterStrike flappyBird = new HelicopterStrike();
            frame.add(flappyBird);
            frame.pack();
            frame.setSize(360, 640);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            flappyBird.requestFocus();
            frame.setVisible(true);
        });
    }
}
