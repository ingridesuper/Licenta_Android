package com.example.licentaagain.repositories;

import android.util.Log;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.ProblemSignature;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.function.Consumer;

public class ProblemSignatureRepository {
    private FirebaseFirestore db;
    public ProblemSignatureRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void addProblemSignature(ProblemSignature newSignature, Consumer<Boolean> callback){
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

    public void removeSignature(String problemId, String userId, Consumer<Boolean> callback){
        db.collection("problem_signatures")
                .whereEqualTo("userId", userId)
                .whereEqualTo("problemId", problemId)
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

    public void problemSignedByUser(String problemId, String userId, Consumer<Boolean> callback) {
        db.collection("problem_signatures")
                .whereEqualTo("userId", userId)
                .whereEqualTo("problemId", problemId)
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
}
