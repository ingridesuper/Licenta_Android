package com.example.licentaagain.repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.ProblemSignature;
import com.example.licentaagain.utils.GDPRDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProblemSignatureRepository {
    private FirebaseFirestore db;
    public ProblemSignatureRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void problemBelongsToUser(String problemId, String uid, Consumer<Boolean> callback){
        db.collection("problems")
                .document(problemId)
                .get()
                .addOnSuccessListener(documentTask -> {
                    DocumentSnapshot problem = documentTask;
                    if (problem.exists() && problem.getString("authorUid").equals(uid)) {
                        callback.accept(true);
                    }
                    else {
                        callback.accept(false);
                    }
                });
    }

    public void addProblemSignature(String problemdId, String uid, Context context, Consumer<Boolean> callback){
        ProblemSignature newSignature=new ProblemSignature(problemdId, uid);
        problemBelongsToUser(problemdId, uid, belongs->{
            if(!belongs){
                db.collection("problem_signatures")
                        .add(newSignature)
                        .addOnSuccessListener(documentReference -> {
                            Log.i("Firestore", "Semnatura adaugata: " + documentReference.getId());
                            //sendNotificationToProblemOwner(problemdId, context);
                            callback.accept(true);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Eroare la adaugarea semnaturii", e);
                            callback.accept(false);
                        });
            }
            else {
                callback.accept(false);
                Toast.makeText(context, "Această problemă vă aparține - deja sunteți pe lista semnatarilor", Toast.LENGTH_LONG).show();
            }
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

    public void numberSignaturesOfProblem(String problemId, Consumer<Integer> callback) {
        db.collection("problem_signatures")
                .whereEqualTo("problemId", problemId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    callback.accept(count);
                })
                .addOnFailureListener(e -> {
                    callback.accept(0);
                });
    }

    //    private void sendNotificationToProblemOwner(String problemId, Context context) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("problems").document(problemId).get()
//                .addOnSuccessListener(problemDoc -> {
//                    if (problemDoc.exists()) {
//                        String authorUid = problemDoc.getString("authorUid");
//
//                        db.collection("users").document(authorUid).get()
//                                .addOnSuccessListener(userDoc -> {
//                                    if (userDoc.exists()) {
//                                        String fcmToken = userDoc.getString("fcmToken");
//                                        if (fcmToken != null && !fcmToken.isEmpty()) {
//                                            sendPushNotification(fcmToken, context, "Semnătură nouă", "Cineva a semnat problema ta.");
//                                        }
//                                    }
//                                });
//                    }
//                });
//    }
//
//    private void sendPushNotification(String fcmToken, Context context, String title, String body) {
//        try {
//            JSONObject notification = new JSONObject();
//            JSONObject notifBody = new JSONObject();
//
//            notifBody.put("title", title);
//            notifBody.put("body", body);
//
//            notification.put("to", fcmToken);
//            notification.put("notification", notifBody);
//
//            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
//                    "https://fcm.googleapis.com/fcm/send",
//                    notification,
//                    response -> Log.d("FCM", "Notificare trimisă: " + response),
//                    error -> Log.e("FCM", "Eroare notificare: " + error.getMessage())
//            ) {
//                @Override
//                public Map<String, String> getHeaders() {
//                    Map<String, String> headers = new HashMap<>();
//                    headers.put("Authorization", "key=AAAA..."); // Server key de la Firebase Console
//                    headers.put("Content-Type", "application/json");
//                    return headers;
//                }
//            };
//
//            Volley.newRequestQueue(context).add(request);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//    }



}
