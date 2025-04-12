package com.example.licentaagain.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.ChipAdapter;
import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterBottomSheet extends BottomSheetDialogFragment {
    private ProblemFilterState currentState;

    private FilterListener listener;

    public interface FilterListener {
        void onFilterApplied(ProblemFilterState newState);
    }

    public FilterBottomSheet(FilterListener listener, ProblemFilterState currentState) {
        this.listener = listener;
        this.currentState=currentState;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_bottom_sheet, container, false);
        makeUIMirrorCurrentState(view);
        subscribeToEvents(view);
        return view;
    }

    private void subscribeToEvents(View view) {
        Button applyFilterButton = view.findViewById(R.id.btnApply);
        applyFilterButton.setOnClickListener(v -> {
            ProblemFilterState newState = new ProblemFilterState();

            RadioGroup radioGroup = view.findViewById(R.id.rgSort);
            int selectedRadioId = radioGroup.getCheckedRadioButtonId();
            if (selectedRadioId == R.id.rbNewest) {
                newState.setSortOrder(ProblemFilterState.SortOrder.NEWEST);
            } else if (selectedRadioId == R.id.rbOldest) {
                newState.setSortOrder(ProblemFilterState.SortOrder.OLDEST);
            }

            List<Sector> selectedSectors = getSelectedItemsFromChipGroup(view, R.id.chipGroupSector);
            List<CategorieProblema> selectedCategories = getSelectedItemsFromChipGroup(view, R.id.chipGroupCategory);

            newState.setSelectedSectors(selectedSectors);
            newState.setSelectedCategories(selectedCategories);

            listener.onFilterApplied(newState);
            dismiss();
        });
    }


    //Helper method to get selected items from a ChipGroup.
    //view - parent view containing the ChipGroup.
    //chipGroupId The ID of the ChipGroup to process.
    // returns the list of selected items from the ChipGroup.
    private <T> List<T> getSelectedItemsFromChipGroup(View view, int chipGroupId) {
        ChipGroup chipGroup = view.findViewById(chipGroupId);
        List<T> selectedItems = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View chipView = chipGroup.getChildAt(i);
            if (chipView instanceof Chip) {
                Chip chip = (Chip) chipView;
                if (chip.isChecked()) {
                    selectedItems.add((T) chip.getTag()); // Retrieves the tagged item! (Sector or CategorieProblema)
                }
            }
        }
        return selectedItems;
    }
    private void makeUIMirrorCurrentState(View view) {
        RadioGroup radioGroup = view.findViewById(R.id.rgSort);
        ChipGroup sectorChipGroup = view.findViewById(R.id.chipGroupSector);
        ChipGroup categoryChipGroup = view.findViewById(R.id.chipGroupCategory);

        if (currentState.getSortOrder() == ProblemFilterState.SortOrder.NEWEST || currentState.getSortOrder() == ProblemFilterState.SortOrder.OLDEST) {
            radioGroup.check(currentState.getSortOrder() == ProblemFilterState.SortOrder.NEWEST ? R.id.rbNewest : R.id.rbOldest);
        }

        //pt sector
        bindChips(sectorChipGroup, Arrays.asList(Sector.values()), currentState.getSelectedSectors(), new ChipAdapter.ChipTextProvider<Sector>() {
            @Override
            public String getChipText(Sector item) {
                return "Sectorul " + item.getNumar();
            }
        });

        //pt categorie
        bindChips(categoryChipGroup, Arrays.asList(CategorieProblema.values()), currentState.getSelectedCategories(), new ChipAdapter.ChipTextProvider<CategorieProblema>() {
            @Override
            public String getChipText(CategorieProblema item) {
                return item.getCategorie();
            }
        });
    }

    private <T> void bindChips(ChipGroup chipGroup, List<T> items, List<T> selectedItems, ChipAdapter.ChipTextProvider<T> chipTextProvider) {
        ChipAdapter<T> chipAdapter = new ChipAdapter<>(getContext(), chipGroup, items, selectedItems, chipTextProvider);
        chipAdapter.bindChips();
    }

}
