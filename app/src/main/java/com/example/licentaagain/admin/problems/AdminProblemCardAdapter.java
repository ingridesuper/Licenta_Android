package com.example.licentaagain.admin.problems;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.admin.AdminPageActivity;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.problem.ProblemDetailsFragment;
import com.example.licentaagain.repositories.ProblemRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.checkerframework.checker.units.qual.A;

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
        holder.itemView.setOnClickListener(v->{
            Context context = v.getContext();

            if (context instanceof AdminPageActivity) {
                AdminPageActivity activity = (AdminPageActivity) context;

                Bundle bundle = new Bundle();
                bundle.putSerializable("problem", problem);

                AdminProblemDetailsFragment adminProblemDetailsFragment = new AdminProblemDetailsFragment();
                adminProblemDetailsFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, adminProblemDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }

        });
    }

    private void setButtonListeners(ProblemViewHolder holder, Problem problem) {
        holder.btnDelete.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Sunteți sigur că vreți să ștergeți această problemă?")
                    .setMessage("Sunteți sigur că vreți să ștergeți această problemă? Ștergerea este ireversibilă.")
                    .setPositiveButton("Șterge", (dialog, id) -> {
                        new ProblemRepository().deleteProblem(problem, result ->{
                            if(!result){
                                Toast.makeText(context, "A apărut o eroare", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
            builder.create().show();
        });

        holder.btnEdit.setOnClickListener(v->{
            if (context instanceof AdminPageActivity) {
                AdminPageActivity activity = (AdminPageActivity) context;

                Bundle bundle = new Bundle();
                bundle.putSerializable("problem", problem);

                AdminEditProblemFragment editProblemFragment = new AdminEditProblemFragment();
                editProblemFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, editProblemFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void fillUiWithData(ProblemViewHolder holder, Problem problem) {
        holder.titleTextView.setText(problem.getTitle());
        holder.addressTextView.setText(problem.getAddress()+", Sector "+problem.getSector());
        holder.stareTextView.setText("Stare: "+problem.getStareProblema());
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
        TextView titleTextView, addressTextView, stareTextView;
        ShapeableImageView imageView;
        MaterialButton btnEdit, btnDelete;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            addressTextView=itemView.findViewById(R.id.tvAddress);
            stareTextView=itemView.findViewById(R.id.tvStare);
            btnEdit=itemView.findViewById(R.id.btnEdit);
            btnDelete=itemView.findViewById(R.id.btnDelete);
            imageView=itemView.findViewById(R.id.ivProblem);
        }
    }
}