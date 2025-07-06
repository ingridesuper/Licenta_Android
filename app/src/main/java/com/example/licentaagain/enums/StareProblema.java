package com.example.licentaagain.enums;

import androidx.annotation.NonNull;

public enum StareProblema {
    CURS_STRANGERE_SEMNATURI("În curs de strângere de semnături"),
    AWAITING_RESPONSE("Trimisă către autorități - în așteptare răspuns"),
    AWAITING_RESOLVATION("Așteptare rezolvare"),
    UNSATISFACTORY("Răspuns nesatisfăcător"),
    SOLVED("Rezolvată")
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

    @NonNull
    @Override
    public String toString() {
        return stare;
    }
}
