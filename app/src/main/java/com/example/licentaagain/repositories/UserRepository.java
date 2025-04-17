package com.example.licentaagain.repositories;

import android.util.Log;

import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.User;
import com.example.licentaagain.view_models.UserViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserRepository {
    private FirebaseFirestore db;

    //obs current user does not appear in searches
    public void searchUserByEmailOrNameSurname(String searchText, String currentUserId ,UserFetchCallback callback) {
        CollectionReference ref = FirebaseFirestore.getInstance().collection("users");
        List<User> fetchedUsers=new ArrayList<>();

        Task<QuerySnapshot> emailQuery = ref.whereGreaterThanOrEqualTo("email", searchText)
                .whereLessThanOrEqualTo("email", searchText + "\uf8ff")
                .get();

        Task<QuerySnapshot> nameQuery = ref.whereGreaterThanOrEqualTo("name", searchText)
                .whereLessThanOrEqualTo("name", searchText + "\uf8ff")
                .get();

        Task<QuerySnapshot> surnameQuery = ref.whereGreaterThanOrEqualTo("surname", searchText)
                .whereLessThanOrEqualTo("surname", searchText + "\uf8ff")
                .get();

        //sau name si surname idk cum

        Tasks.whenAllComplete(emailQuery, nameQuery, surnameQuery).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (Task<?> individualTask : task.getResult()) {
                    if (individualTask.isSuccessful()) {
                        QuerySnapshot result = (QuerySnapshot) individualTask.getResult();
                        for (DocumentSnapshot doc : result.getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (!fetchedUsers.contains(user) && !user.getUid().equals(currentUserId)) { //based on uid
                                fetchedUsers.add(user);
                            }
                        }
                    }
                }
                Log.i("fetchedUsers", String.valueOf(fetchedUsers.size())+": "+fetchedUsers.toString());
                callback.onFetchComplete(fetchedUsers);

            } else {
                Log.e("FirestoreSearch", "Error retrieving search results", task.getException());
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

}
