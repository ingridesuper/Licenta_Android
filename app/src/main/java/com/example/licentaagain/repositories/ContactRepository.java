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

}
