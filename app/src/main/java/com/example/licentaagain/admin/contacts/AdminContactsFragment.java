package com.example.licentaagain.admin.contacts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.repositories.ContactRepository;

import java.util.List;

public class AdminContactsFragment extends Fragment {
    private RecyclerView rvContacts;
    private ContactAdapter adapter;


    public AdminContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvContacts = view.findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        new ContactRepository().getDateContact(
                contactList -> {
                    adapter = new ContactAdapter(contactList);
                    rvContacts.setAdapter(adapter);
                },
                error -> {
                    Log.e("FirestoreError", "Eroare la încărcarea contactelor", error);
                }
        );
    }

}