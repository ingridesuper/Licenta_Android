package com.example.licentaagain.admin.contacts;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.models.DateContact;
import com.example.licentaagain.repositories.ContactRepository;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminContactsFragment extends Fragment {
    private RecyclerView rvContacts;
    private ContactAdapter adapter;
    private List<DateContact> fullContactList = new ArrayList<>();



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
        setUpRecyclerView(view);
        setUpAiPrompt(view);
        setUpSearchEvents(view);
        subscribeEditEvent(view);
    }

    private void subscribeEditEvent(View view) {
        Button btnEdit = view.findViewById(R.id.btnEdit);
        TextView tvPrompt = view.findViewById(R.id.tvPrompt);

        btnEdit.setOnClickListener(v -> {
            final EditText input = new EditText(getContext());
            input.setText(tvPrompt.getText().toString());

            new AlertDialog.Builder(requireContext())
                    .setTitle("Editează Prompt AI")
                    .setView(input)
                    .setPositiveButton("Salvează", (dialog, which) -> {
                        String newPrompt = input.getText().toString().trim();
                        if (!newPrompt.isEmpty()) {
                            new ContactRepository().updateAiPrompt(
                                    newPrompt,
                                    () -> {
                                        tvPrompt.setText(newPrompt); // Actualizează local
                                        Log.d("AI Prompt", "Prompt actualizat cu succes");
                                    },
                                    error -> Log.e("FirestoreError", "Eroare la salvarea promptului", error)
                            );
                        }
                    })
                    .setNegativeButton("Anulează", null)
                    .show();
        });
    }


    private void setUpSearchEvents(View view) {
        SearchView searchView=view.findViewById(R.id.searchBar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        });
    }

    private void setUpAiPrompt(View view) {
        TextView tvPrompt= view.findViewById(R.id.tvPrompt);
        new ContactRepository().getAiDestinationPrompt(
                prompt->tvPrompt.setText(prompt),
                error->Log.e("FirestoreError", "Eroare la încărcarea contactelor", error)
        );
    }
    private void setUpRecyclerView(@NonNull View view) {
        rvContacts = view.findViewById(R.id.rvContacts);

        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        new ContactRepository().getDateContact(
                contactList -> {
                    fullContactList=contactList;
                    adapter = new ContactAdapter(contactList);
                    rvContacts.setAdapter(adapter);
                },
                error ->
                    Log.e("FirestoreError", "Eroare la încărcarea contactelor", error)
        );
    }

    private void filterContacts(String query) {
        List<DateContact> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (DateContact contact : fullContactList) {
            String institutie = contact.getInstitutie();
            String email = contact.getEmail();
            String locatie = contact.getLocatie();

            if ((institutie != null && institutie.toLowerCase().contains(lowerCaseQuery)) ||
                    (email != null && email.toLowerCase().contains(lowerCaseQuery)) ||
                    (locatie != null && locatie.toLowerCase().contains(lowerCaseQuery))) {
                filteredList.add(contact);
            }
        }

        adapter.updateData(filteredList);
    }


}