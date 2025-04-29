package com.example.licentaagain.custom_adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.problem.EditProblemFragment;
import com.example.licentaagain.problem.ProblemOfCurrentUserDetailsFragment;
import com.example.licentaagain.repositories.ProblemRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ProblemsByCurrentUserCardAdapter extends RecyclerView.Adapter<ProblemsByCurrentUserCardAdapter.ProblemByUserViewHolder> {
    private Context context;

    private List<Problem> problemList;

    public ProblemsByCurrentUserCardAdapter(Context context, List<Problem> problemList) {
        this.context = context;
        this.problemList = problemList;
    }

    public ProblemByUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.problem_by_current_user_card_item, parent, false);
        return new ProblemsByCurrentUserCardAdapter.ProblemByUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemsByCurrentUserCardAdapter.ProblemByUserViewHolder holder, int position) {
        Problem problem = problemList.get(position);
        fillUiWithData(holder, problem);
        setButtonListeners(holder, problem);
        setOnProblemClickListener(holder, problem);
    }

    private void setOnProblemClickListener(ProblemByUserViewHolder holder, Problem problem) {
        holder.itemView.setOnClickListener(v->{
            Context context = v.getContext();

            if (context instanceof HomePageActivity) {
                HomePageActivity activity = (HomePageActivity) context;

                Bundle bundle = new Bundle();
                bundle.putSerializable("problem", problem);

                ProblemOfCurrentUserDetailsFragment problemDetailsFragment = new ProblemOfCurrentUserDetailsFragment();
                problemDetailsFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, problemDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setButtonListeners(ProblemByUserViewHolder holder, Problem problem) {
        holder.btnEdit.setOnClickListener(v->{
            if (context instanceof HomePageActivity) {
                HomePageActivity activity = (HomePageActivity) context;

                Bundle bundle = new Bundle();
                bundle.putSerializable("problem", problem);

                EditProblemFragment editProblemFragment = new EditProblemFragment();
                editProblemFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, editProblemFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

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
                        StareProblema stareProblemaNoua = StareProblema.fromString(stareSelectata); //view model aici
                        new ProblemRepository().updateStareProblema(problem.getId(), stareProblemaNoua);
                        dialog.dismiss();
                    })

                    .setNegativeButton("Anulează", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });

    }

    private void fillUiWithData(@NonNull ProblemsByCurrentUserCardAdapter.ProblemByUserViewHolder holder, Problem problem) {
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

    public static class ProblemByUserViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, addressTextView, categoryTextView;
        ShapeableImageView imageView;
        MaterialButton btnEdit;
        Button btnUpdateStare;

        public ProblemByUserViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            addressTextView=itemView.findViewById(R.id.tvAddress);
            categoryTextView=itemView.findViewById(R.id.tvCategory);
            btnEdit=itemView.findViewById(R.id.btnEdit);
            btnUpdateStare=itemView.findViewById(R.id.btnUpdateStare);
            imageView=itemView.findViewById(R.id.ivProblem);
        }
    }

    public void updateData(List<Problem> newProblems) {
        this.problemList = newProblems;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }
}
