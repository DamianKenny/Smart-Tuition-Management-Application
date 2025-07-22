package com.nibm.tuitonmanagementapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.tuitonmanagementapplication.model.Assignment;

import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    public interface OnAssignmentClickListener {
        void onAssignmentClick(Assignment assignment);
    }

    private final List<Assignment> assignments;
    private final OnAssignmentClickListener listener;

    public AssignmentAdapter(List<Assignment> assignments, OnAssignmentClickListener listener) {
        this.assignments = assignments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.assignment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);
        holder.titleTextView.setText(assignment.getTitle());
        holder.courseTextView.setText(assignment.getCourse());
        holder.descriptionTextView.setText(assignment.getDescription());

        // Set click listener for the View PDF button
        holder.viewPdfButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignmentClick(assignment);
            }
        });
    }


    @Override
    public int getItemCount() {
        return assignments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        public final TextView courseTextView;
        public final TextView descriptionTextView;
        public final Button viewPdfButton; // Add this

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            courseTextView = itemView.findViewById(R.id.courseTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            viewPdfButton = itemView.findViewById(R.id.viewPdfButton); // Add this
        }
    }

}