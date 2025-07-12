package com.example.licentaagain.account.account_info;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.auth.LoginActivity;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.repositories.UserRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAccountFragment extends Fragment {
    FirebaseFirestore db;
    FirebaseAuth auth;

    TextInputEditText etNume, etPrenume;
    Spinner spnSector;
    Button btnSave, btnDeleteAccount;
    String oldName, oldSurname;
    int oldSector;
    String email;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_account, container, false);

        getDataFromBundle();
        initializeVariables(view);
        populateSpinner();
        fillWithOriginalData();
        subscribeToEvents();

        return view;
    }

    private void getDataFromBundle(){
        Bundle bundle = getArguments();
        if (bundle != null) {
            oldSurname = bundle.getString("nume", "");
            oldName = bundle.getString("prenume", "");
            oldSector = bundle.getInt("sector", 0);
            email = bundle.getString("email", "");

            Log.i("info bundle", oldName+" "+oldSurname+" "+oldSector+" "+email);
        }
    }
    private void initializeVariables(View view){
        auth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        etNume=view.findViewById(R.id.etSurname);
        etPrenume=view.findViewById(R.id.etName);
        spnSector=view.findViewById(R.id.spnSector);
        btnSave=view.findViewById(R.id.btnSave);
        btnDeleteAccount=view.findViewById(R.id.btnDeleteAccount);
    }

    private void populateSpinner(){
        ArrayAdapter<Sector> arrayAdapterSpinner=new ArrayAdapter<Sector>(requireActivity(), R.layout.spinner_item, Sector.values());
        spnSector.setAdapter(arrayAdapterSpinner);
    }

    private void fillWithOriginalData(){
        etNume.setText(oldName);
        etPrenume.setText(oldSurname);
        for (int i = 0; i < Sector.values().length; i++) {
            if (Sector.values()[i].getNumar() == oldSector) {
                spnSector.setSelection(i);
                break;
            }
        }
    }

    private void subscribeToEvents(){
        btnSave.setOnClickListener(v-> updateFirestoreDB());
        btnDeleteAccount.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Confirmare ștergere cont");
            builder.setMessage("Sunteți sigur că doriți ștergerea contului? Toate semnăturile și problemele dumneavoastră vor fi șterse ireversibil.\nScrieți „Da, sunt sigur” pentru a confirma ștergerea contului.");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Șterge", (dialog, which) -> {
                String confirmationText = input.getText().toString().trim();
                if (confirmationText.equals("Da, sunt sigur")) {
                    new UserRepository().deleteUser(auth.getUid(), result -> {
                        if (result) {
                            FirebaseAuth.getInstance().signOut(); // delogare
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // curata backstack
                            startActivity(intent);
                            Toast.makeText(requireContext(), "Cont șters cu succes", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            Toast.makeText(requireContext(), "Eroare la ștergerea contului", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Textul introdus nu este corect. Contul nu a fost șters.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Anulează", (dialog, which) -> dialog.cancel());

            builder.show();
        });

    }

    private void updateFirestoreDB(){
        String newNume=etNume.getText().toString();
        String newPrenume=etPrenume.getText().toString();
        Sector selectedSector = (Sector) spnSector.getSelectedItem();
        int newSector=selectedSector.getNumar();

        String userId=auth.getCurrentUser().getUid();
        DocumentReference userRef=db.collection("users").document(userId);
        userRef.update("name", newPrenume, "surname", newNume, "sector", newSector)
                .addOnSuccessListener(aVoid->{
                    Log.i("Firestore Update", "Datele au fost actualizate cu succes.");
                    goBackToAccountPage();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Eroare la actualizare: " + e.getMessage());
                    Toast.makeText(requireActivity(), "Eroare la actualizare.", Toast.LENGTH_SHORT).show();
                });

    }

    private void goBackToAccountPage(){
        requireActivity().getSupportFragmentManager().popBackStack(); //popBackStack - returns to previous fragment
    }


}
