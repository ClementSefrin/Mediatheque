package doc;

import java.io.Serial;

public class EmpruntException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    //TODO : faire une exception quand un mineur réserve ou emprunt un film pour adulte (
    public EmpruntException() {
        super("Vous n'avez pas le droit d'emprunter ce document.");
    }
}
