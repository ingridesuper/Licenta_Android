package com.example.licentaagain.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EmailHelper {

        public static void sendEmailWithAttachments(Context context, String subject, String body, List<File> attachmentFiles) {
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("message/rfc822"); // pentru aplicații de email

                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);

                ArrayList<Uri> attachmentUris = new ArrayList<>();
                for (File file : attachmentFiles) {
                    Uri contentUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".provider", // foarte important
                            file
                    );
                    attachmentUris.add(contentUri);
                }

                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUris);
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // foarte important pentru ca app-ul de email sa poata citi atașamentele

                context.startActivity(Intent.createChooser(emailIntent, "Trimite email cu..."));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Nu s-a găsit o aplicație de email sau a apărut o eroare.", Toast.LENGTH_SHORT).show();
            }
        }
    }

