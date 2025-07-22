package com.nibm.tuitonmanagementapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nibm.tuitonmanagementapplication.model.Teacher;
import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private final List<Teacher> teacherList;
    private final OnTeacherClickListener listener;

    public interface OnTeacherClickListener {
        void onTeacherClick(Teacher teacher);
    }

    public TeacherAdapter(List<Teacher> teacherList, OnTeacherClickListener listener) {
        this.teacherList = teacherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = teacherList.get(position);

        holder.tvFullName.setText(teacher.getFullName());
        holder.tvEmail.setText(teacher.getEmail());
        holder.tvPhone.setText(teacher.getPhone());

        // Handle optional fields
        if (teacher.getSubject() != null && !teacher.getSubject().isEmpty()) {
            holder.tvSubject.setText(teacher.getSubject());
        } else {
            holder.tvSubject.setText("Not specified");
        }

        if (teacher.getQualification() != null && !teacher.getQualification().isEmpty()) {
            holder.tvQualification.setText(teacher.getQualification());
        } else {
            holder.tvQualification.setText("Not specified");
        }

        if (teacher.getExperience() != null && !teacher.getExperience().isEmpty()) {
            holder.tvExperience.setText(teacher.getExperience());
        } else {
            holder.tvExperience.setText("Not specified");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTeacherClick(teacher);
            }
        });
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvPhone, tvSubject, tvQualification, tvExperience;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvQualification = itemView.findViewById(R.id.tvQualification);
            tvExperience = itemView.findViewById(R.id.tvExperience);
        }
    }
}