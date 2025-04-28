package com.example.licentaagain.repositories;

import android.telecom.Call;
import android.util.Log;

import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class UserRepository {
    private FirebaseFirestore db;

    //obs current user does not appear in searches
    public void searchUserByEmailOrNameSurname(String searchText, String currentUserId ,UserFetchCallback callback) {
//        CollectionReference ref = FirebaseFirestore.getInstance().collection("users");
//        List<User> fetchedUsers=new ArrayList<>();
//
//        Task<QuerySnapshot> emailQuery = ref.whereGreaterThanOrEqualTo("email", searchText)
//                .whereLessThanOrEqualTo("email", searchText + "\uf8ff")
//                .get();
//
//        Task<QuerySnapshot> nameQuery = ref.whereGreaterThanOrEqualTo("name", searchText)
//                .whereLessThanOrEqualTo("name", searchText + "\uf8ff")
//                .get();
//
//        Task<QuerySnapshot> surnameQuery = ref.whereGreaterThanOrEqualTo("surname", searchText)
//                .whereLessThanOrEqualTo("surname", searchText + "\uf8ff")
//                .get();
//
//        //sau name si surname idk cum
//
//        Tasks.whenAllComplete(emailQuery, nameQuery, surnameQuery).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                for (Task<?> individualTask : task.getResult()) {
//                    if (individualTask.isSuccessful()) {
//                        QuerySnapshot result = (QuerySnapshot) individualTask.getResult();
//                        for (DocumentSnapshot doc : result.getDocuments()) {
//                            User user = doc.toObject(User.class);
//                            if (!fetchedUsers.contains(user) && !user.getUid().equals(currentUserId)) { //based on uid
//                                fetchedUsers.add(user);
//                            }
//                        }
//                    }
//                }
//                Log.i("fetchedUsers", String.valueOf(fetchedUsers.size())+": "+fetchedUsers.toString());
//                callback.onFetchComplete(fetchedUsers);
//
//            } else {
//                Log.e("FirestoreSearch", "Error retrieving search results", task.getException());
//            }
//        });

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<User> fetchedUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot user : task.getResult()) {
                            String name=user.getString("name"); //un user poate sa nu le aiba setate
                            String surname=user.getString("surname");
                            String email=user.getString("email");
                            String nameSurname=name+ " "+surname;
                            String surnameName=surname+" "+name;
                            if(name!=null && surname!=null && email!=null){
                                if(name.toLowerCase().contains(searchText.toLowerCase()) ||
                                        surname.toLowerCase().contains(searchText.toLowerCase()) ||
                                        email.toLowerCase().contains(searchText.toLowerCase()) ||
                                        nameSurname.toLowerCase().contains(searchText) ||
                                        surnameName.toLowerCase().contains(searchText)
                                ) {
                                    User newUser = user.toObject(User.class);
                                    fetchedUsers.add(newUser);
                                }
                            }

                        }
                        callback.onFetchComplete(fetchedUsers);
                    }
                    else {
                        Log.e("User Search Error", "user search error");
                    }
                });
    }

    public void getUserBasedOnId(String uid, Consumer<User> callback) {
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot doc : task.getResult()){
                            User user=doc.toObject(User.class);
                            callback.accept(user);
                            Log.i("userA", user.toString());
                        }
                    }
                    else {
                        Log.e("userNameError", "Error fetching "+task.getException());
                        callback.accept(null);
                    }
                });
    }

    public interface UserFetchCallback {
        void onFetchComplete(List<User> users);
    }
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

    public void checkIfUserHasNameSurnameSectorData(String uid, Consumer<Boolean> callback){
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot doc : task.getResult()){
                            User user=doc.toObject(User.class);
                            if(user.getSurname()!=null && user.getName()!=null && user.getSector()!=0){
                                callback.accept(true);
                            }
                            else {
                                callback.accept(false);
                            }
                        }
                    }
                    else {
                        Log.e("checkIfUserHasNameSurnameSectorData", "User complete data check failed: "+task.getException());
                    }
                });
    }

    public void getUsersWhoSignedProblem(String problemId, Consumer<List<User>> callback) {
        db.collection("problem_signatures")
                .whereEqualTo("problemId", problemId)
                .get()
                .addOnSuccessListener(signaturesSnapshot -> {
                    List<String> userIds = new ArrayList<>();
                    for (QueryDocumentSnapshot signature : signaturesSnapshot) {
                        String userId = signature.getString("userId");
                        if (userId != null) {
                            userIds.add(userId);
                        }
                    }

                    if (userIds.isEmpty()) {
                        callback.accept(new ArrayList<>()); // No users signed
                        return;
                    }

                    List<User> users = new ArrayList<>();
                    AtomicInteger completed = new AtomicInteger(0);
                    for (String userId : userIds) {
                        db.collection("users")
                                .whereEqualTo("uid", userId)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {
                                    for (QueryDocumentSnapshot userDoc : userSnapshot) {
                                        User user = userDoc.toObject(User.class);
                                        users.add(user);
                                    }
                                    if (completed.incrementAndGet() == userIds.size()) {
                                        callback.accept(users);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Failed to fetch user " + userId, e);
                                    if (completed.incrementAndGet() == userIds.size()) {
                                        callback.accept(users);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to fetch problem signatures", e);
                    callback.accept(new ArrayList<>());
                });
    }


}
