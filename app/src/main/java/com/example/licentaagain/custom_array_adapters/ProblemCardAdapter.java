package com.example.licentaagain.custom_array_adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Visibility;

import com.example.licentaagain.R;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.Problem;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.function.Consumer;

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
        holder.addressTextView.setText(problem.getAddress()+", Sector "+problem.getSector());
        holder.categoryTextView.setText("Categorie: "+problem.getCategorieProblema());
        problemSignedByUser(problem, isSigned -> {
            if(isSigned){
                holder.btnSign.setVisibility(View.INVISIBLE);
                holder.btnSigned.setVisibility(View.VISIBLE);
            }
            else {
                holder.btnSign.setVisibility(View.VISIBLE);
                holder.btnSigned.setVisibility(View.INVISIBLE);
            }
        });
    }


    //Consumer<Boolean> - interfata functionala Java ce primeste Boolean
    //o folosim pt calback pt ca operatiile sunt asincrone
    private void problemSignedByUser(Problem problem, Consumer<Boolean> callback) {
        String currentUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.collection("problem_signatures")
                .whereEqualTo("userId", currentUid)
                .whereEqualTo("problemId", problem.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(task.getResult().isEmpty()){
                            callback.accept(false);
                        }
                        else {
                            callback.accept(true);
                        }
                    }
                    else {
                        Log.e("semnaturi", "eroare");
                        callback.accept(false);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return problemList.size();
    }

    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, addressTextView, categoryTextView;
        MaterialButton btnSign, btnSigned;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            addressTextView=itemView.findViewById(R.id.tvAddress);
            categoryTextView=itemView.findViewById(R.id.tvCategory);
            btnSign=itemView.findViewById(R.id.btnSign);
            btnSigned=itemView.findViewById(R.id.btnSigned);
        }
    }
}
