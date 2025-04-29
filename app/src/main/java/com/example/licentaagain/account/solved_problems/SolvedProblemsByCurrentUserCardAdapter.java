package com.example.licentaagain.account.solved_problems;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licentaagain.R;
import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.models.Problem;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class SolvedProblemsByCurrentUserCardAdapter extends RecyclerView.Adapter<SolvedProblemsByCurrentUserCardAdapter.ProblemViewHolder> {

    private Context context;

    private List<Problem> problemList;
    SolvedProblemsByCurrentUserViewModel viewModel;


    public SolvedProblemsByCurrentUserCardAdapter(Context context, List<Problem> problemList, SolvedProblemsByCurrentUserViewModel viewModel) {
        this.context = context;
        this.problemList = problemList;
        this.viewModel=viewModel;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.solved_problem_by_current_user_card, parent, false);
        return new SolvedProblemsByCurrentUserCardAdapter.ProblemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        Problem problem=problemList.get(position);
        fillUiWithData(holder, problem);
        setChangeStareButtonClickListener(holder, problem);
    }

    private void setChangeStareButtonClickListener(ProblemViewHolder holder, Problem problem) {
        holder.btnUpdateStare.setOnClickListener(v -> {
            List<String> stari = new ArrayList<>();
            for (StareProblema stare : StareProblema.values()) {
                stari.add(stare.getStare());
            }
            final int[] selectedItem = {stari.indexOf(problem.getStareProblema())};

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Schimbă starea acestei probleme")
                    .setSingleChoiceItems(stari.toArray(new String[0]), selectedItem[0], (dialog, which) -> {
                        selectedItem[0] = which;
                    })
                    .setPositiveButton("Confirmă", (dialog, which) -> {
                        String stareSelectata = stari.get(selectedItem[0]);
                        problem.setStareProblema(stareSelectata);
                        StareProblema stareProblemaNoua = StareProblema.fromString(stareSelectata);
                        viewModel.updateStareProblema(problem, stareProblemaNoua);
                        dialog.dismiss();
                    })

                    .setNegativeButton("Anulează", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
    }

    private void fillUiWithData(ProblemViewHolder holder, Problem problem) {
        holder.titleTextView.setText(problem.getTitle());
        holder.addressTextView.setText(problem.getAddress()+", Sector "+problem.getSector());
        holder.categoryTextView.setText("Categorie: "+problem.getCategorieProblema());
        if(!problem.getImageUrls().isEmpty()){
            String imageUrl = problem.getImageUrls().get(0);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.imageView);
            Log.i("image",problem.getImageUrls().get(0));
        }
        else {
            Log.i("image","empty");
        }
    }

    @Override
    public int getItemCount() {
        return problemList.size();
    }


    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, addressTextView, categoryTextView;
        ShapeableImageView imageView;
        Button btnUpdateStare;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            addressTextView=itemView.findViewById(R.id.tvAddress);
            categoryTextView=itemView.findViewById(R.id.tvCategory);
            btnUpdateStare=itemView.findViewById(R.id.btnUpdateStare);
            imageView=itemView.findViewById(R.id.ivProblem);
        }
    }

    public void updateData(List<Problem> newProblems) {
        this.problemList = newProblems;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }
}
