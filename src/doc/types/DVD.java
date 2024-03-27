package doc.types;

import doc.Document;

public class DVD extends Document {
    private final boolean adulte;

    public DVD(int numero, String titre, boolean adulte) {
        super(numero, titre);
        this.adulte = adulte;
    }

    public boolean estAdulte() {
        return this.adulte;
    }

    @Override
    public String toString() {
        return adulte ? super.toString() + " | Pour adulte" : super.toString();
    }

}
