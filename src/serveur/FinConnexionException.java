package serveur;

import java.io.Serial;

public class FinConnexionException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public FinConnexionException(String message) {
        super(message);
    }

    public String toString() {
        return getMessage();
    }
}
