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

    public static StareProblema fromString(String text) {
        for (StareProblema s : StareProblema.values()) {
            if (s.stare.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Nu exista starea: " + text);
    }

    @Override
    public String toString() {
        return "StareProblema{" +
                "stare='" + stare + '\'' +
                '}';
    }
}
