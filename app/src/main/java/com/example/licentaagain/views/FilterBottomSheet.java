package com.example.licentaagain.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_array_adapters.SectorChipAdapter;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

            if(((RadioGroup)view.findViewById(R.id.rgSort)).getCheckedRadioButtonId()==R.id.rbNewest){
                newState.setSortOrder(ProblemFilterState.SortOrder.NEWEST);
            }
            else if(((RadioGroup)view.findViewById(R.id.rgSort)).getCheckedRadioButtonId()==R.id.rbOldest){
                newState.setSortOrder(ProblemFilterState.SortOrder.OLDEST);
            }

            newState.setSelectedSectors(currentState.getSelectedSectors());

            listener.onFilterApplied(newState);
            dismiss();
        });
    }

    private void makeUIMirrorCurrentState(View view) {
        RadioGroup radioGroup = view.findViewById(R.id.rgSort);
        ChipGroup sectorChipGroup=view.findViewById(R.id.chipGroupSector);
        if(currentState.getSortOrder() == ProblemFilterState.SortOrder.NEWEST || currentState.getSortOrder() == ProblemFilterState.SortOrder.OLDEST){
            radioGroup.check(currentState.getSortOrder() == ProblemFilterState.SortOrder.NEWEST ? R.id.rbNewest : R.id.rbOldest);
        }

        List<Sector> selectedSectors = currentState.getSelectedSectors();
        SectorChipAdapter sectorChipAdapter = new SectorChipAdapter(getContext(), sectorChipGroup, Arrays.asList(Sector.values()), selectedSectors);
        sectorChipAdapter.bindChips();
    }

}
