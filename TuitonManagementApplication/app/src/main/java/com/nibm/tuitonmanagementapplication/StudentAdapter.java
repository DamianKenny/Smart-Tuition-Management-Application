package com.nibm.tuitonmanagementapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.tuitonmanagementapplication.model.Student;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<Student> studentList;

    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        holder.tvFullName.setText(student.getFullName());
        holder.tvEmail.setText(student.getEmail());

        // Handle optional fields
        if (student.getModule() != null && !student.getModule().isEmpty()) {
            holder.tvModule.setText(student.getModule());
        } else {
            holder.tvModule.setText("Not assigned");
        }

        if (student.getBatch() != null && !student.getBatch().isEmpty()) {
            holder.tvBatch.setText(student.getBatch());
        } else {
            holder.tvBatch.setText("Not assigned");
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvModule, tvBatch;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvModule = itemView.findViewById(R.id.tvModule);
            tvBatch = itemView.findViewById(R.id.tvBatch);
        }
    }
}