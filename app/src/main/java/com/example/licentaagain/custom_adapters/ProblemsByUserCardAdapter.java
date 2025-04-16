package com.example.licentaagain.custom_adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.problem.EditProblemFragment;
import com.example.licentaagain.problem.ProblemDetailsFragment;
import com.example.licentaagain.repositories.ProblemRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ProblemsByUserCardAdapter extends RecyclerView.Adapter<ProblemsByUserCardAdapter.ProblemByUserViewHolder> {
    private Context context;

    private List<Problem> problemList;
    private ProblemRepository problemRepository;

    public ProblemsByUserCardAdapter(Context context, List<Problem> problemList) {
        this.context = context;
        this.problemList = problemList;
        problemRepository=new ProblemRepository();
    }

    public ProblemByUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each problem
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.problem_by_user_card_item, parent, false);
        return new ProblemsByUserCardAdapter.ProblemByUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemsByUserCardAdapter.ProblemByUserViewHolder holder, int position) {
        Problem problem = problemList.get(position);
        fillUiWithData(holder, problem);
        Log.i("bind", String.valueOf(position));
        setButtonListeners(holder, problem);
        //setOnProblemClickListener(holder, problem);
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
    }

    private void fillUiWithData(@NonNull ProblemsByUserCardAdapter.ProblemByUserViewHolder holder, Problem problem) {
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

        public ProblemByUserViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            addressTextView=itemView.findViewById(R.id.tvAddress);
            categoryTextView=itemView.findViewById(R.id.tvCategory);
            btnEdit=itemView.findViewById(R.id.btnEdit);
            imageView=itemView.findViewById(R.id.ivProblem);
        }
    }

    public void updateData(List<Problem> newProblems) {
        this.problemList = newProblems;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }
}
