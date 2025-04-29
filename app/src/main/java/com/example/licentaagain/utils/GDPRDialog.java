package com.example.licentaagain.utils;

import android.app.AlertDialog;
import android.content.Context;

import com.example.licentaagain.R;

public class GDPRDialog {
    public static void showGDPRDialog(Context context, final Runnable onAgreeCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Acord GDPR")
                .setMessage(context.getString(R.string.gdpr))
                .setPositiveButton("Sunt de acord", (dialog, id) -> onAgreeCallback.run())
                .setNegativeButton("Nu sunt de acord", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }
}
