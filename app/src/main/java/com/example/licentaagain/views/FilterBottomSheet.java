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
import com.example.licentaagain.utils.ProblemFilterState;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterBottomSheet extends BottomSheetDialogFragment {
    private RadioGroup radioGroup;
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
        radioGroup = view.findViewById(R.id.rgSort);
        Button applyFilterButton = view.findViewById(R.id.btnApply);

        //aici facem UI-ul sa reflecte ViewModel-ul!
        if(currentState.getSortOrder() == ProblemFilterState.SortOrder.NEWEST || currentState.getSortOrder() == ProblemFilterState.SortOrder.OLDEST){
            radioGroup.check(currentState.getSortOrder() == ProblemFilterState.SortOrder.NEWEST ? R.id.rbNewest : R.id.rbOldest);
        }

        applyFilterButton.setOnClickListener(v -> {
            ProblemFilterState newState = new ProblemFilterState();

            if(radioGroup.getCheckedRadioButtonId()==R.id.rbNewest){ //il las asa nu cu operator ternar -> pt ca vreau sa se poata sa nu fie nimic selectat
                newState.setSortOrder(ProblemFilterState.SortOrder.NEWEST);
            }
            else if(radioGroup.getCheckedRadioButtonId()==R.id.rbOldest){
                newState.setSortOrder(ProblemFilterState.SortOrder.OLDEST);
            }

            listener.onFilterApplied(newState);
            dismiss(); // close bottom sheet
        });

        return view;
    }
}
