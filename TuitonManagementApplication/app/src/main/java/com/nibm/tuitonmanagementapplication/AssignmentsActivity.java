package com.nibm.tuitonmanagementapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.tuitonmanagementapplication.model.Student;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentsActivity extends RecyclerView.Adapter<AssignmentsActivity.AssignmentViewHolder> {

    private final List<Student> assignmentList;

    public AssignmentsActivity(List<Student> assignmentList) {
        this.assignmentList = assignmentList;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Student assignment = assignmentList.get(position);
        holder.bind(assignment);
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView textStudentName;
        private final TextView textStudentBatch;
        private final TextView textTeacher;
        private final TextView textCourse;
        private final TextView textDate;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textStudentName = itemView.findViewById(R.id.textStudentName);
            textStudentBatch = itemView.findViewById(R.id.textStudentBatch);
            textTeacher = itemView.findViewById(R.id.textTeacher);
            textCourse = itemView.findViewById(R.id.textCourse);
            textDate = itemView.findViewById(R.id.textDate);
        }

        public void bind(Student assignment) {
            // Student name with null check
            textStudentName.setText(assignment.getStudentName() != null ?
                    assignment.getStudentName() : "Student Name Not Available");

            // Batch and module with null checks
            String batch = assignment.getStudentBatch() != null ?
                    assignment.getStudentBatch() : "N/A";
            String module = assignment.getStudentModule() != null ?
                    assignment.getStudentModule() : "N/A";
            textStudentBatch.setText(String.format("Batch: %s | Module: %s", batch, module));

            // Teacher info with null checks
            String teacherName = assignment.getTeacherName() != null ?
                    assignment.getTeacherName() : "N/A";
            String teacherSubject = assignment.getTeacherSubject() != null ?
                    assignment.getTeacherSubject() : "N/A";
            textTeacher.setText(String.format("Teacher: %s (%s)", teacherName, teacherSubject));

            // Course name with null check
            String courseName = assignment.getCourseName() != null ?
                    assignment.getCourseName() : "Course Not Assigned";
            textCourse.setText(String.format("Course: %s", courseName));

            // Date formatting with null check
            if (assignment.getAssignmentDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDate = sdf.format(new Date(assignment.getAssignmentDate()));
                textDate.setText(String.format("Assigned: %s", formattedDate));
            } else {
                textDate.setText("Assignment Date Not Available");
            }
        }
    }
}
