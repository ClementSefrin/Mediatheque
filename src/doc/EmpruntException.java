package doc;

import java.io.Serial;

public class EmpruntException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    public EmpruntException(String message) {
        super(message);
    }

    public String toString() {
        return getMessage();
    }
}
