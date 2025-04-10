package com.example.licentaagain.utils;

public class ProblemFilterState { //practic doar o clasa care ma ajuta sa tin minte tot ce e selectat
    //o folosesc mostly pt UI experience -> ca atunci cand apas pe filter sa imi apara exact pe baza a ce e filtrat tot
    //o am in problem view model ca live data model
    public enum SortOrder{
        NONE, NEWEST, OLDEST
    }

    private SortOrder sortOrder;
    //and more here


    public ProblemFilterState() {
        this.sortOrder = SortOrder.NONE;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
}
