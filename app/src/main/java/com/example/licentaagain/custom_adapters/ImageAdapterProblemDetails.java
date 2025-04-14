package com.example.licentaagain.custom_adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licentaagain.R;

import java.util.List;

public class ImageAdapterProblemDetails extends androidx.recyclerview.widget.RecyclerView.Adapter<ImageAdapterProblemDetails.ImageViewHolder> {
    private Context context;
    private List<String> problemUris;  // List of image URLs

    public ImageAdapterProblemDetails(Context context, List<String> problemUris) {
        this.context = context;
        this.problemUris = problemUris;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = problemUris.get(position);

        // Load the image into the ImageView using Glide
        Glide.with(context)
                .load(imageUrl)  // Load the image URL from Firebase Storage
                .centerCrop()  // Crop the image to fill the ImageView
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return problemUris.size();  // Return the number of images
    }

    public static class ImageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}