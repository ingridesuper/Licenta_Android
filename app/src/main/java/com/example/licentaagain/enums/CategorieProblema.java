package com.example.licentaagain.enums;

import androidx.annotation.NonNull;

public enum CategorieProblema {
    GUNOI("Gunoi"),
    RECICLARE("Reciclare nerespectata sau inadecvata"),
    TRECERE_PIETONI("Trecere de pietoni neconforma"),
    PISTA_BICICLETA("Pista de bicicleta blocata sau "),
    GROAPA("Groapa in asfalt, groapa etc"),
    GURA_CANALIZARE("Gura de canalizare sparta, deteriorata etc"),
    PARCARE("Parcare ilegala"),
    ACCESIBILITATE("Accesibilitate"),
    SPATII_VERZI("Spatii verzi"),
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
