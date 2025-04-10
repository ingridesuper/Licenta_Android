package com.example.licentaagain.custom_array_adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Visibility;

import com.example.licentaagain.R;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.ProblemSignature;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.function.Consumer;

public class ProblemCardAdapter extends RecyclerView.Adapter<ProblemCardAdapter.ProblemViewHolder> {

    private List<Problem> problemList;

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
        // Binds the problem data to the CardView
        Problem problem = problemList.get(position);
        fillUiWithData(holder, problem);
        setButtonListeners(holder, problem);
    }

    private void setButtonListeners(ProblemViewHolder holder, Problem problem) {
        holder.btnSign.setOnClickListener(v ->
                addSignature(problem, success -> updateButtonVisibility(holder, true))
        );

        holder.btnSigned.setOnClickListener(v ->
                removeSignature(problem, success -> updateButtonVisibility(holder, false))
        );
    }

    private void updateButtonVisibility(ProblemViewHolder holder, boolean isSigned) {
        holder.btnSign.setVisibility(isSigned ? View.INVISIBLE : View.VISIBLE);
        holder.btnSigned.setVisibility(isSigned ? View.VISIBLE : View.INVISIBLE);
    }

    private void fillUiWithData(@NonNull ProblemViewHolder holder, Problem problem) {
    holder.titleTextView.setText(problem.getTitle());
    holder.addressTextView.setText(problem.getAddress()+", Sector "+problem.getSector());
    holder.categoryTextView.setText("Categorie: "+problem.getCategorieProblema());
    problemSignedByUser(problem, isSigned -> {
        updateButtonVisibility(holder, isSigned);
    });
}

    private void addSignature(Problem problem, Consumer<Boolean> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ProblemSignature newSignature=new ProblemSignature(problem.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("problem_signatures")
                .add(newSignature)
                .addOnSuccessListener(documentReference -> {
                    Log.i("Firestore", "Semnatura adaugata: " + documentReference.getId());
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Eroare la adaugarea semnaturii", e);
                    callback.accept(false);
                });
    }

    private void removeSignature(Problem problem, Consumer<Boolean> callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("problem_signatures")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereEqualTo("problemId", problem.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            db.collection("problem_signatures").document(doc.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.i("Firestore", "Semnatura eliminata");
                                        callback.accept(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Eroare la stergerea semnaturii", e);
                                        callback.accept(false);
                                    });
                        }
                    } else {
                        callback.accept(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Eroare la cautarea semnaturii", e);
                    callback.accept(false);
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


    //am definit viewholder aici, pt ca e specific adaptorului
    //e static inner class
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

    public void updateData(List<Problem> newProblems) {
        this.problemList = newProblems;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }

}
