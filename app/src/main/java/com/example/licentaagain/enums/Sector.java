package com.example.licentaagain.enums;

public enum Sector {
    UNU(1),
    DOI(2),
    TREI(3),
    PATRU(4),
    CINCI(5),
    SASE(6);

    private final int numar;

    Sector(int numar) {
        this.numar = numar;
    }
    public int getNumar() {
        return this.numar;
    }
}
