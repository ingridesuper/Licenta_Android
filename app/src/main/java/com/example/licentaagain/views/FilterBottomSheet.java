package com.example.licentaagain.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.licentaagain.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterBottomSheet extends BottomSheetDialogFragment {
    private RadioGroup radioGroup;

    private FilterListener listener;

    public interface FilterListener {
        void onFilterApplied(boolean newestSelected, boolean oldestSelected);
    }

    public FilterBottomSheet(FilterListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_bottom_sheet, container, false);

        radioGroup = view.findViewById(R.id.rgSort);
        Button applyFilterButton = view.findViewById(R.id.btnApply);
        applyFilterButton.setOnClickListener(v -> {
            listener.onFilterApplied(radioGroup.getCheckedRadioButtonId()==R.id.rbNewest, radioGroup.getCheckedRadioButtonId()==R.id.rbOldest);
            dismiss(); // close bottom sheet
        });

        return view;
    }
}
