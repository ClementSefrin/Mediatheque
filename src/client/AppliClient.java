package client;

import javax.swing.*;
import java.awt.*;

public class AppliClient {
    private static final int PORT_RESERVATION = 3000;
    private static final int PORT_EMPRUNT_RETOUR = 4000;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppliClient::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Client Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Utilisation de BoxLayout pour aligner les composants verticalement
        frame.getContentPane().add(panel);

        JLabel label = new JLabel("Que voulez-vous faire ?");
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Aligner le texte au centre horizontalement
        panel.add(label);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Utilisation de FlowLayout pour disposer les boutons côte-à-côte
        panel.add(buttonPanel);

        JButton reservationButton = new JButton("Reserver un document");
        reservationButton.addActionListener(e -> startClient(PORT_RESERVATION));
        buttonPanel.add(reservationButton);

        JButton empruntRetourButton = new JButton("Emprunter/Retourner un document");
        empruntRetourButton.addActionListener(e -> startClient(PORT_EMPRUNT_RETOUR));
        buttonPanel.add(empruntRetourButton);

        frame.pack();
        frame.setVisible(true);
    }

    private static void startClient(int port) {
        Thread clientThread = new Thread(() -> Client.main(new String[]{Integer.toString(port)}));
        clientThread.start();
    }
}
