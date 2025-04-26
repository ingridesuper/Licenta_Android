package com.example.licentaagain.utils;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageHelper {

    public static void downloadFiles(Context context, List<String> imageUrls, OnDownloadCompleteListener listener) {
        List<File> downloadedFiles = new ArrayList<>();

        for (String url : imageUrls) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);

            File localFile = new File(context.getCacheDir(), UUID.randomUUID().toString() + ".jpg");

            ref.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        downloadedFiles.add(localFile);

                        if (downloadedFiles.size() == imageUrls.size()) {
                            listener.onDownloadComplete(downloadedFiles);
                        }
                    })
                    .addOnFailureListener(e -> {
                        listener.onDownloadFailed(e);
                    });
        }
    }

    public interface OnDownloadCompleteListener {
        void onDownloadComplete(List<File> downloadedFiles);
        void onDownloadFailed(Exception e);
    }

}
