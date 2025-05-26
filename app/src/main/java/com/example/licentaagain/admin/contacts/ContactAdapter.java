package com.example.licentaagain.admin.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaagain.R;
import com.example.licentaagain.models.DateContact;
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
        holder.tvInstitutie.setText(contact.getInstitutie() != null ? contact.getInstitutie() : "");
        holder.tvEmail.setText(contact.getEmail() != null ? contact.getEmail() : "");
        holder.tvTelefon.setText(contact.getTelefon() != null ? contact.getTelefon() : "");
        holder.tvExtra.setText(contact.getExtra() != null ? contact.getExtra() : "");


        holder.btnEdit.setOnClickListener(v -> {
            // ceva
        });

        holder.btnDelete.setOnClickListener(v->{

        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstitutie, tvEmail, tvTelefon, tvExtra;
        MaterialButton btnEdit, btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstitutie = itemView.findViewById(R.id.tvInstitutie);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvExtra=itemView.findViewById(R.id.tvExtra);
            tvTelefon = itemView.findViewById(R.id.tvTelefon);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete=itemView.findViewById(R.id.btnDelete);
        }
    }
}

