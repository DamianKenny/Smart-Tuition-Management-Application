package com.nibm.tuitonmanagementapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.tuitonmanagementapplication.model.Material;

import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    private final List<Material> materials;
    private final Context context;

    public MaterialAdapter(List<Material> materials, Context context) {
        this.materials = materials;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.material_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material material = materials.get(position);

        holder.titleTextView.setText(material.getTitle());
        holder.moduleTextView.setText("Module: " + material.getModule());
        holder.uploadedDateTextView.setText("Uploaded Date: " + material.getUploadedDate());
        holder.submissionDateTextView.setText("Submission Date: " + material.getSubmissionDate());

        // Set click listener for the View button
        holder.viewButton.setOnClickListener(v -> openMaterial(material.getFileUrl()));
    }

    private void openMaterial(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(context, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Determine MIME type based on file extension
            String mimeType = "application/pdf";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(fileUrl), mimeType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Verify that there's an app to handle the intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // If no app is available, open in browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        public final TextView moduleTextView;
        public final TextView uploadedDateTextView;
        public final TextView submissionDateTextView;
        public final Button viewButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            moduleTextView = itemView.findViewById(R.id.tvModule);
            uploadedDateTextView = itemView.findViewById(R.id.tvUploadedDate);
            submissionDateTextView = itemView.findViewById(R.id.tvSubmissionDate);
            viewButton = itemView.findViewById(R.id.btnView);
        }
    }
}