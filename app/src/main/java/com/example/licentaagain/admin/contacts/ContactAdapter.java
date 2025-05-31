package com.example.licentaagain.admin.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaagain.R;
import com.example.licentaagain.models.DateContact;
import com.example.licentaagain.repositories.ContactRepository;
import com.google.android.material.button.MaterialButton;

import java.util.List;
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<DateContact> contactList;

    public ContactAdapter(List<DateContact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_row, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        DateContact contact = contactList.get(position);
        holder.tvInstitutie.setText(contact.getInstitutie() != null && !contact.getInstitutie().isEmpty() ? contact.getInstitutie() : "Instituție: -");
        holder.tvEmail.setText(contact.getEmail() != null  && !contact.getEmail().isEmpty() ? "Email: "+contact.getEmail() : "Email: -");
        holder.tvTelefon.setText(contact.getTelefon() != null  && !contact.getTelefon().isEmpty() ? "Telefon: "+contact.getTelefon() : "Telefon: -");
        holder.tvExtra.setText(contact.getExtra() != null  && !contact.getExtra().isEmpty() ? "Informații extra: "+contact.getExtra() : "Informații extra: -");

        holder.btnEdit.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(v.getContext());
            View dialogView = inflater.inflate(R.layout.dialog_edit_contact, null);

            EditText etInstitutie = dialogView.findViewById(R.id.etEditInstitutie);
            EditText etLocatie=dialogView.findViewById(R.id.etLocatie);
            EditText etEmail = dialogView.findViewById(R.id.etEditEmail);
            EditText etTelefon = dialogView.findViewById(R.id.etEditTelefon);
            EditText etExtra = dialogView.findViewById(R.id.etEditExtra);

            // Pre-populare
            etInstitutie.setText(contact.getInstitutie());
            etEmail.setText(contact.getEmail());
            etLocatie.setText(contact.getLocatie());
            etTelefon.setText(contact.getTelefon());
            etExtra.setText(contact.getExtra());

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Editează contact")
                    .setView(dialogView)
                    .setPositiveButton("Salvează", (dialog, which) -> {
                        contact.setInstitutie(etInstitutie.getText().toString());
                        contact.setLocatie(etLocatie.getText().toString());
                        contact.setEmail(etEmail.getText().toString());
                        contact.setTelefon(etTelefon.getText().toString());
                        contact.setExtra(etExtra.getText().toString());

                        new ContactRepository().updateContact(contact,
                                success -> {
                                    if (success) {
                                        notifyItemChanged(position);
                                        Toast.makeText(v.getContext(), "Contact actualizat", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(v.getContext(), "Eroare la actualizare", Toast.LENGTH_SHORT).show();
                                    }
                                },
                                fail -> Toast.makeText(v.getContext(), "Eroare la conectare", Toast.LENGTH_SHORT).show()
                        );
                    })
                    .setNegativeButton("Anulează", null)
                    .show();

        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstitutie, tvEmail, tvTelefon, tvExtra;
        MaterialButton btnEdit;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstitutie = itemView.findViewById(R.id.tvInstitutie);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTelefon = itemView.findViewById(R.id.tvTelefon);
            tvExtra = itemView.findViewById(R.id.tvExtra);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    public void updateData(List<DateContact> newList) {
        this.contactList = newList;
        notifyDataSetChanged();
    }
}
