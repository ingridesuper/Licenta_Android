package com.example.licentaagain.utils;

import com.example.licentaagain.enums.Sector;

import java.util.ArrayList;
import java.util.List;

public class ProblemFilterState { //practic doar o clasa care ma ajuta sa tin minte tot ce e selectat
    //o folosesc mostly pt UI experience -> ca atunci cand apas pe filter sa imi apara exact pe baza a ce e filtrat tot
    //o am in problem view model ca live data model
    public enum SortOrder{
        NONE, NEWEST, OLDEST
    }

    private SortOrder sortOrder;
    private List<Sector> sectorList;
    //and more here


    public ProblemFilterState() {
        this.sortOrder = SortOrder.NONE;
        sectorList =new ArrayList<>();
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Sector> getSectorList() {
        return sectorList;
    }

    public void setSectorList(List<Sector> sectorList) {
        this.sectorList = sectorList;
    }

    public void addSector(Sector sector){
        this.sectorList.add(sector);
    }
}
