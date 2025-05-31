package com.example.licentaagain.repositories;

import com.example.licentaagain.models.DateContact;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ContactRepository {
    private FirebaseFirestore db;

    public ContactRepository() {
        db=FirebaseFirestore.getInstance();
    }

    public void getDateContact(Consumer<List<DateContact>> onSuccess, Consumer<Exception> onFailure) {
        db.collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DateContact> contactList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        DateContact contact = document.toObject(DateContact.class);
                        contactList.add(contact);
                    }
                    onSuccess.accept(contactList);
                })
                .addOnFailureListener(onFailure::accept);
    }

    public void getAiDestinationPrompt(Consumer<String> onSuccess, Consumer<Exception> onFailure){
        db.collection("static_variables")
                .whereEqualTo("purpose", "ai_email_destination_prompt")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String prompt = queryDocumentSnapshots.getDocuments()
                                .get(0)
                                .getString("text");
                        onSuccess.accept(prompt != null ? prompt : "");
                    } else {
                        onSuccess.accept("");
                    }
                })
                .addOnFailureListener(onFailure::accept);
    }


    public void updateAiPrompt(String newPrompt, Runnable onSuccess, Consumer<Exception> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("static_variables")
                .whereEqualTo("purpose", "ai_email_destination_prompt")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String docId = document.getId();

                        db.collection("static_variables")
                                .document(docId)
                                .update("text", newPrompt)
                                .addOnSuccessListener(aVoid -> onSuccess.run())
                                .addOnFailureListener(onFailure::accept);
                    } else {
                        onFailure.accept(new Exception("Document not found"));
                    }
                })
                .addOnFailureListener(onFailure::accept);
    }

}
