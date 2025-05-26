package com.example.licentaagain.admin.contacts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.repositories.ContactRepository;

import java.util.List;

public class AdminContactsFragment extends Fragment {


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
        TextView tvContact=view.findViewById(R.id.tvContacts);
        new ContactRepository().getDateContact(
                contactlist->{
                    tvContact.setText(contactlist.toString());
                },
                error->{
                    Log.e("FirestoreError", "Eroare la încărcarea contactelor", error);
                }
        );
    }
}