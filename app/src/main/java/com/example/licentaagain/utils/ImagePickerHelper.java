package com.example.licentaagain.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImagePickerHelper {
    private final Fragment fragment;
    private final int MAX_IMAGES = 5;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private final List<Uri> selectedImageUris;
    private final ImagePickerCallback callback;

    public interface ImagePickerCallback {
        void onImagesSelected(List<Uri> imageUris);
        void onImageCaptureFailed(String error);
    }

    public ImagePickerHelper(Fragment fragment, List<Uri> selectedImageUris, ImagePickerCallback callback) {
        this.fragment = fragment;
        this.selectedImageUris = selectedImageUris;
        this.callback = callback;
        registerCameraLauncher();
        registerGalleryLauncher();
    }

    private void registerCameraLauncher() {
        takePictureLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && cameraImageUri != null) {
                        if (selectedImageUris.size() < MAX_IMAGES) {
                            selectedImageUris.add(cameraImageUri);
                            callback.onImagesSelected(selectedImageUris);
                        } else {
                            callback.onImageCaptureFailed("Maximum number of images reached");
                        }
                    }
                }
        );
    }

    private void registerGalleryLauncher() {
        imagePickerLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ClipData clipData = result.getData().getClipData();
                        selectedImageUris.clear();
                        if (clipData != null) {
                            int count = Math.min(clipData.getItemCount(), MAX_IMAGES);
                            for (int i = 0; i < count; i++) {
                                selectedImageUris.add(clipData.getItemAt(i).getUri());
                            }
                        } else {
                            selectedImageUris.add(result.getData().getData());
                        }
                        callback.onImagesSelected(selectedImageUris);
                    }
                }
        );
    }

    public void openCamera() {
        if (selectedImageUris.size() >= MAX_IMAGES) {
            callback.onImageCaptureFailed("Maximum number of images reached");
            return;
        }

        File imageFile;
        try {
            imageFile = File.createTempFile("IMG_", ".jpg", fragment.requireContext().getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onImageCaptureFailed("Could not create image file");
            return;
        }

        cameraImageUri = FileProvider.getUriForFile(
                fragment.requireContext(),
                fragment.requireContext().getPackageName() + ".provider",
                imageFile
        );

        takePictureLauncher.launch(cameraImageUri);
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }


}

