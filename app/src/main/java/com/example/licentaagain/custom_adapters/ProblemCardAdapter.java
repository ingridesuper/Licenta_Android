package com.example.licentaagain.custom_adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.ProblemSignature;
import com.example.licentaagain.models.User;
import com.example.licentaagain.problem.ProblemDetailsFragment;
import com.example.licentaagain.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.function.Consumer;

public class ProblemCardAdapter extends RecyclerView.Adapter<ProblemCardAdapter.ProblemViewHolder> {
    private Context context;

    private List<Problem> problemList;

    public ProblemCardAdapter(Context context, List<Problem> problemList) {
        this.context=context;
        this.problemList = problemList;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each problem
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ProblemViewHolder(view);
    }


    //leaga datele dintr-un Problem concret la cardul asta
    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        Problem problem = problemList.get(position);
        fillUiWithData(holder, problem);
        setButtonListeners(holder, problem);
        setOnProblemClickListener(holder, problem);
    }

    private void setOnProblemClickListener(@NonNull ProblemViewHolder holder, Problem problem) {
        holder.itemView.setOnClickListener(v->{
            Context context = v.getContext(); //activitatea curenta practic

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
        new UserRepository().checkIfUserHasNameSurnameSectorData(FirebaseAuth.getInstance().getCurrentUser().getUid(), result->{
            if(result){
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
            else{
                Toast.makeText(context, "Completati-va profilul pentru a putea semna!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //astea trebuie mutate intr-un repository!
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
        //view holder e specific recycler view pt imbunatatire performanta
        //e practic doar o cutie cu referintele astea, ca sa nu apelam la fiecare binding
        //legat de onBindViewHolder de mai sus!
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
