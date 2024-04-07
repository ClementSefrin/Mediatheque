package doc.types;

import doc.Document;

public class Livre extends Document {
    private final int nbPages;

    public Livre(int numero, String titre, int nbPages) {
        super(numero, titre);
        this.nbPages = nbPages;
    }

    @Override
    public String toString() {
        return super.toString() + " | Pages : " + nbPages;
    }
}
