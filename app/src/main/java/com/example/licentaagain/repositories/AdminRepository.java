package com.example.licentaagain.repositories;

import android.util.Log;

import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.models.Problem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AdminRepository { //de adaugat checks ca current auth e tip admin -> la toate functiile
    private FirebaseFirestore db;

    public AdminRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public ListenerRegistration listenToAllProblems(Consumer<List<Problem>> callback){
        return db.collection("problems")
                .addSnapshotListener((snapshots, e)->{
                    if (e != null) {
                        Log.e("Firestore", "Listen failed.", e);
                        return;
                    }
                    assert snapshots!=null;
                    List<Problem> problems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Problem newProblem = new Problem(
                                doc.getString("address"),
                                doc.getString("authorUid"),
                                doc.getString("description"),
                                doc.getDouble("latitude"),
                                doc.getDouble("longitude"),
                                doc.getDouble("sector").intValue(),
                                doc.getString("title"),
                                doc.getString("categorieProblema"),
                                (List<String>) doc.get("imageUrls"),
                                StareProblema.fromString(doc.getString("stareProblema")),
                                doc.getString("facebookGroupLink")
                        );
                        newProblem.setId(doc.getId());
                        problems.add(newProblem);
                    }
                    callback.accept(problems);

                });
    }

    //aici nu uita sa adaugi starea -> cand iei cp din probelm repo
    //pt with picture change
    public void updateProblemWithoutPictureChange(String problemId, Problem newProblem, Consumer<Boolean> callback){
        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("title", newProblem.getTitle());
        updatedFields.put("description", newProblem.getDescription());
        updatedFields.put("sector", newProblem.getSector());
        updatedFields.put("categorieProblema", newProblem.getCategorieProblema());
        updatedFields.put("stareProblema", newProblem.getStareProblema());
        updatedFields.put("latitude", newProblem.getLatitude());
        updatedFields.put("longitude", newProblem.getLongitude());
        updatedFields.put("address", newProblem.getAddress());
        updatedFields.put("facebookGroupLink", newProblem.getFacebookGroupLink());
        db.collection("problems")
                .document(problemId)
                .update(updatedFields)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.accept(true);
                    } else {
                        callback.accept(false);
                    }
                });
    }

}
