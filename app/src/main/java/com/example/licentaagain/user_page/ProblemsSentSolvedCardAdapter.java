package com.example.licentaagain.user_page;

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
import com.example.licentaagain.account.reported_problems.sent.SentProblemDetailsFragment;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.problem.ProblemDetailsFragment;
import com.example.licentaagain.problem.SentSolvedProblemDetailsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ProblemsSentSolvedCardAdapter extends RecyclerView.Adapter<ProblemsSentSolvedCardAdapter.ProblemViewHolder> {
    private Context context;

    private List<Problem> problemList;

    public ProblemsSentSolvedCardAdapter(Context context, List<Problem> problemList) {
        this.context = context;
        this.problemList = problemList;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_solved_card_item, parent, false);
        return new ProblemsSentSolvedCardAdapter.ProblemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        Problem problem=problemList.get(position);
        fillUiWithData(holder, problem);
        setOnProblemClickListener(holder, problem);
    }

    private void setOnProblemClickListener(ProblemViewHolder holder, Problem problem) {
        holder.itemView.setOnClickListener(v->{
            Context context = v.getContext();

            if (context instanceof HomePageActivity) {
                HomePageActivity activity = (HomePageActivity) context;

                Bundle bundle = new Bundle();
                bundle.putSerializable("problem", problem);

                SentSolvedProblemDetailsFragment problemDetailsFragment = new SentSolvedProblemDetailsFragment();
                problemDetailsFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, problemDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }


    @Override
    public int getItemCount() {
        return problemList.size();
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

    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, addressTextView, categoryTextView;
        ShapeableImageView imageView;
        MaterialButton btnSign, btnSigned;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            addressTextView=itemView.findViewById(R.id.tvAddress);
            categoryTextView=itemView.findViewById(R.id.tvCategory);
            btnSign=itemView.findViewById(R.id.btnSign);
            btnSigned=itemView.findViewById(R.id.btnSigned);
            imageView=itemView.findViewById(R.id.ivProblem);
        }
    }

    public void updateData(List<Problem> newProblems) {
        this.problemList = newProblems;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }
}
