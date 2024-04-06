package client;

import java.util.Scanner;

public class AppliClient {
    public static void main(String[] args) { //TODO : réussir à ouvrir un nouveau terminal pour chaque client
        Scanner scanner = new Scanner(System.in);
        int nbClients = 0;

        while (true) {
            System.out.print("Voulez-vous ajouter un nouveau client ? (oui/non) : ");
            String response = scanner.next();

            if (response.equalsIgnoreCase("non")) {
                break;
            } else if (!response.equalsIgnoreCase("oui")) {
                System.out.println("Veuillez répondre par 'oui' ou 'non'");
                continue;
            }

            System.out.print("Entrez le port pour le nouveau client : ");
            int port = scanner.nextInt();
            Thread clientThread = new Thread(() -> Client.main(new String[]{Integer.toString(port)}));
            clientThread.start();
            nbClients++;
        }

        System.out.println("Nombre total de clients démarrés : " + nbClients);
    }
}
