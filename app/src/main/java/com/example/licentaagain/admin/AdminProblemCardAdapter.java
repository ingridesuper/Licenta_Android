package com.example.licentaagain.admin;

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
import com.example.licentaagain.models.Problem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class AdminProblemCardAdapter extends RecyclerView.Adapter<AdminProblemCardAdapter.ProblemViewHolder> {

    private Context context;

    private List<Problem> problemList;

    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_problem_card_item, parent, false);
        return new AdminProblemCardAdapter.ProblemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProblemCardAdapter.ProblemViewHolder holder, int position) {
        Problem problem=problemList.get(position);
        fillUiWithData(holder, problem);
        setButtonListeners(holder, problem);
        setOnProblemClickListener(holder, problem);
    }

    private void setOnProblemClickListener(ProblemViewHolder holder, Problem problem) {
    }

    private void setButtonListeners(ProblemViewHolder holder, Problem problem) {
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

    public AdminProblemCardAdapter(Context context, List<Problem> problemList){
        this.context=context;
        this.problemList=problemList;
    }

    public void updateData(List<Problem> newProblems) {
        this.problemList = newProblems;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }

    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, addressTextView, categoryTextView;
        ShapeableImageView imageView;
        MaterialButton btnEdit, btnDelete;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            addressTextView=itemView.findViewById(R.id.tvAddress);
            categoryTextView=itemView.findViewById(R.id.tvCategory);
            btnEdit=itemView.findViewById(R.id.btnEdit);
            btnDelete=itemView.findViewById(R.id.btnDelete);
            imageView=itemView.findViewById(R.id.ivProblem);
        }
    }
}