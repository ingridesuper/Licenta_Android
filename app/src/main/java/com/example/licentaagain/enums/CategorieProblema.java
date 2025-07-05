package com.example.licentaagain.enums;

import androidx.annotation.NonNull;

public enum CategorieProblema {
    GUNOI("Gunoi"),
    RECICLARE("Reciclare neconformă"),
    TRECERE_PIETONI("Trecere de pietoni"),
    PISTA_BICICLETA("Pistă de bicicletă"),
    GROAPA("Groapă în asfalt"),
    GURA_CANALIZARE("Gură de canalizare deteriorată"),
    PARCARE("Parcare ilegală"),
    ACCESIBILITATE("Accesibilitate"),
    SPATII_VERZI("Spații verzi"),
    ILUMINAT("Iluminat stradal"),
    ALTCEVA("Altceva");

    private String categorie;

    CategorieProblema(String categorie) {
        this.categorie = categorie;
    }

    public String getCategorie() {
        return categorie;
    }


    @NonNull
    @Override
    public String toString() {
        return categorie;
    }
}
