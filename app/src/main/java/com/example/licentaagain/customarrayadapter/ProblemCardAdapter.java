package com.example.licentaagain.customarrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaagain.R;
import com.example.licentaagain.models.Problem;

import java.util.List;

public class ProblemCardAdapter extends RecyclerView.Adapter<ProblemCardAdapter.ProblemViewHolder> {

    private List<Problem> problemList;

    // Constructor
    public ProblemCardAdapter(List<Problem> problemList) {
        this.problemList = problemList;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each problem
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ProblemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        // Bind the problem data to the CardView
        Problem problem = problemList.get(position);
        holder.titleTextView.setText(problem.getTitle());
    }

    @Override
    public int getItemCount() {
        return problemList.size();
    }

    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
        }
    }
}
