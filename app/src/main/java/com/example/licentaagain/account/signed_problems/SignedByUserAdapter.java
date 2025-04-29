package com.example.licentaagain.account.signed_problems;

import android.content.Context;
import android.os.Bundle;
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
import com.example.licentaagain.problem.ProblemDetailsFragment;
import com.example.licentaagain.repositories.ProblemSignatureRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SignedByUserAdapter extends RecyclerView.Adapter<SignedByUserAdapter.ProblemViewHolder> {
    private Context context;

    private List<Problem> problemList;

    private SignaturesOfUserViewModel viewModel;
    private ProblemSignatureRepository repository;

    public SignedByUserAdapter(Context context, List<Problem> problemList, SignaturesOfUserViewModel viewModel) {
        this.context = context;
        this.problemList = problemList;
        repository=new ProblemSignatureRepository();
        this.viewModel=viewModel;
    }

    public SignedByUserAdapter.ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new SignedByUserAdapter.ProblemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        Problem problem = problemList.get(position);
        fillUiWithData(holder, problem);
        setButtonListeners(holder, problem);
        setOnProblemClickListener(holder, problem);
    }

    private void setOnProblemClickListener(SignedByUserAdapter.ProblemViewHolder holder, Problem problem) {
        holder.itemView.setOnClickListener(v->{
            Context context = v.getContext();

            if (context instanceof HomePageActivity) {
                HomePageActivity activity = (HomePageActivity) context;

                Bundle bundle = new Bundle();
                bundle.putSerializable("problem", problem);

                ProblemDetailsFragment problemDetailsFragment = new ProblemDetailsFragment();
                problemDetailsFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, problemDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }



    private void setButtonListeners(SignedByUserAdapter.ProblemViewHolder holder, Problem problem) {
        holder.btnSigned.setOnClickListener(v ->
                viewModel.unSignProblem(problem, FirebaseAuth.getInstance().getCurrentUser().getUid())
        );
    }
    private void fillUiWithData(@NonNull SignedByUserAdapter.ProblemViewHolder holder, Problem problem) {
        holder.titleTextView.setText(problem.getTitle());
        holder.addressTextView.setText(problem.getAddress()+", Sector "+problem.getSector());
        holder.categoryTextView.setText("Categorie: "+problem.getCategorieProblema());
        if(!problem.getImageUrls().isEmpty()){
            String imageUrl = problem.getImageUrls().get(0);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return problemList.size();
    }

    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        //view holder e specific recycler view pt imbunatatire performanta
        //e practic doar o cutie cu referintele astea, ca sa nu apelam la fiecare binding
        //legat de onBindViewHolder de mai sus!
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
