package com.example.licentaagain.repositories;

import android.util.Log;

import com.example.licentaagain.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.function.Consumer;

public class UserRepository {
    private FirebaseFirestore db;
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getUserNameSurnameBasedOnId(String uid, Consumer<String> callback){
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot doc : task.getResult()){
                            User user=doc.toObject(User.class);
                            callback.accept(user.getName()+" "+user.getSurname());
                        }
                    }
                    else {
                        Log.e("userNameError", "Error fetching "+task.getException());
                    }
                });
    }
}
