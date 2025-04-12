package com.example.licentaagain.custom_adapters;

import android.content.Context;
import android.view.LayoutInflater;

import com.example.licentaagain.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ChipAdapter<T> {
    private Context context;
    private ChipGroup chipGroup;
    private List<T> items;
    private List<T> selectedItems;
    private ChipTextProvider<T> chipTextProvider;

    public interface ChipTextProvider<T> { //avem text diferit in functie de T
        String getChipText(T item);
    }

    // Constructor for ChipAdapter
    public ChipAdapter(Context context, ChipGroup chipGroup, List<T> items, List<T> selectedItems, ChipTextProvider<T> chipTextProvider) {
        this.context = context;
        this.chipGroup = chipGroup;
        this.items = items;
        this.selectedItems = selectedItems;
        this.chipTextProvider = chipTextProvider;
    }

    // Bind chips to the ChipGroup
    public void bindChips() {
        chipGroup.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (T item : items) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_enum, chipGroup, false);
            chip.setText(chipTextProvider.getChipText(item));
            chip.setTag(item);
            chip.setChecked(selectedItems.contains(item));
            chipGroup.addView(chip);
        }
    }
}
