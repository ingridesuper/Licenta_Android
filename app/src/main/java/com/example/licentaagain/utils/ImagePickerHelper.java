package com.example.licentaagain.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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

                        int remainingSlots = MAX_IMAGES - selectedImageUris.size();
                        if (remainingSlots <= 0) {
                            Toast.makeText(fragment.requireContext(), "Maximum number of images reached", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (clipData != null) {
                            int countToAdd = Math.min(clipData.getItemCount(), remainingSlots);
                            for (int i = 0; i < countToAdd; i++) {
                                selectedImageUris.add(clipData.getItemAt(i).getUri());
                            }
                        } else {
                            Uri singleUri = result.getData().getData();
                            if (singleUri != null && selectedImageUris.size() < MAX_IMAGES) {
                                selectedImageUris.add(singleUri);
                            } else {
                                Toast.makeText(fragment.requireContext(), "Maximum number of images reached", Toast.LENGTH_SHORT).show();
                            }
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

    public void enableDragAndDrop(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                Collections.swap(selectedImageUris, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    public static boolean isRemoteUri(Uri uri) {
        return uri.toString().startsWith("http");
    }


}

