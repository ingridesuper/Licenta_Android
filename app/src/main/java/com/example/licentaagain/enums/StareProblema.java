package com.example.licentaagain.enums;

public enum StareProblema {
    CURS_STRANGERE_SEMNATURI("In curs de strangere de semnaturi"),
    AWAITING_RESPONSE("Trimisa catre autoritati - in asteptare raspuns"),
    AWAITING_RESOLVATION("Asteptarea rezolvare"),
    UNSATISFACTORY("Raspuns nesatisfactor"),
    SOLVED("Rezolvata")
    ;
    private String stare;

    StareProblema(String stare) {
        this.stare = stare;
    }

    public String getStare() {
        return stare;
    }

    @Override
    public String toString() {
        return "StareProblema{" +
                "stare='" + stare + '\'' +
                '}';
    }
}
