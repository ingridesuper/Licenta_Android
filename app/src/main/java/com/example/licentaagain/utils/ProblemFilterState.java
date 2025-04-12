package com.example.licentaagain.utils;

import com.example.licentaagain.enums.CategorieProblema;
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
    private List<Sector> selectedSectors;
    private List<CategorieProblema> selectedCategories;
    //and more here


    public ProblemFilterState() {
        this.sortOrder = SortOrder.NONE;
        selectedSectors =new ArrayList<>();
        selectedCategories=new ArrayList<>();
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Sector> getSelectedSectors() {
        return selectedSectors;
    }

    public void setSelectedSectors(List<Sector> sectorList) {
        this.selectedSectors = sectorList;
    }

    public void addSector(Sector sector){
        this.selectedSectors.add(sector);
    }

    public List<CategorieProblema> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(List<CategorieProblema> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    @Override
    public String toString() {
        return "ProblemFilterState{" +
                "selectedCategories=" + selectedCategories +
                ", sortOrder=" + sortOrder +
                ", selectedSectors=" + selectedSectors +
                '}';
    }
}
