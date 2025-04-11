package com.example.licentaagain.custom_array_adapters;

import android.content.Context;
import android.view.LayoutInflater;

import com.example.licentaagain.R;
import com.example.licentaagain.enums.Sector;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
public class SectorChipAdapter {
    private Context context;
    private ChipGroup chipGroup;
    private List<Sector> sectors;
    private List<Sector> selectedSectors;

    public SectorChipAdapter(Context context, ChipGroup chipGroup, List<Sector> sectors, List<Sector> selectedSectors) {
        this.context = context;
        this.chipGroup = chipGroup;
        this.sectors = sectors;
        this.selectedSectors = selectedSectors;
    }

    // bind chips to ChipGroup dynamically
    public void bindChips() {
        chipGroup.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (Sector sector : sectors) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_sector, chipGroup, false);
            chip.setText("Sectorul " + sector.getNumar());
            chip.setTag(sector); // for later reference!

            chip.setChecked(selectedSectors.contains(sector));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedSectors.add(sector);
                } else {
                    selectedSectors.remove(sector);
                }
            });

            chipGroup.addView(chip);
        }
    }
}


