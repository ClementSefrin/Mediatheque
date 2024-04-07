package client;

//import java.io.IOException;
import java.util.Scanner;

public class AppliClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int nbClients = 0;

        while (true) {
            System.out.print("Voulez-vous ajouter un nouveau client ? (oui/non) : ");
            String response = scanner.next();

            if (response.equalsIgnoreCase("non")) {
                break;
            } else if (!response.equalsIgnoreCase("oui")) {
                System.out.println("Veuillez repondre par 'oui' ou 'non'");
                continue;
            }

            System.out.print("Entrez le port pour le nouveau client : ");
            int port = scanner.nextInt();
            Thread clientThread = new Thread(() -> Client.main(new String[]{Integer.toString(port)}));
            clientThread.start();
            nbClients++;

            //TODO : ouvrir chaque client dans un terminal separe
            /*String command;
            switch (getOS()) {
                case "mac":
                    command = "open -a Terminal -n /bin/bash -c \"java -cp /Users/cpx/Desktop/PÉRIODE C/ProjetArchiLog/"
                            + "Mediatheque/src/client client.Client " + port + "\"";
                    break;
                case "windows":
                    command = "cmd.exe /c start java client.Client " + port;
                    break;
                case "linux":
                    command = "x-terminal-emulator -e java client.Client " + port;
                    break;
                default:
                    System.err.println("Systeme d'exploitation non supporte.");
                    return;
            }

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
                processBuilder.start();
                nbClients++;
            } catch (IOException e) {
                System.err.println("Erreur lors de l'ouverture d'un nouveau terminal : " + e.getMessage());
            }*/
        }

        System.out.println("Nombre total de clients demarres : " + nbClients);
    }

    /*private static String getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        return switch (osName) {
            case "mac os x", "macos", "darwin" -> "mac";
            case "windows" -> "windows";
            case "linux" -> "linux";
            default -> "unknown";
        };
    }*/
}