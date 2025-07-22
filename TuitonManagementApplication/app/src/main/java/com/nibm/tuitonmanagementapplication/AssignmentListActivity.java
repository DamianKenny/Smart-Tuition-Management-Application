package com.nibm.tuitonmanagementapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.tuitonmanagementapplication.model.Student;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AssignmentAdapter adapter;
    private List<Student> assignmentList;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView menuButton = findViewById(R.id.menuButton);
        NavigationView navigationView = findViewById(R.id.nav_view);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        initializeViews();
        setupFirestore();
        loadAssignments();

        //hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_student) {
                startActivity(new Intent(this, ManageStudentsActivity.class));
            } else if (id == R.id.nav_teachers) {
                startActivity(new Intent(this, ManageTeachersActivity.class));
            } else if (id == R.id.nav_reports) {
                //startActivity(new Intent(this, ReportsActivity.class));
            } else if (id == R.id.nav_assignment) {
                startActivity(new Intent(this, StudentAssignmentActivity.class));
            } else if (id == R.id.nav_view_assignment) {
                startActivity(new Intent(this, AssignmentListActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                Intent intent = new Intent(AssignmentListActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewAssignments);
        assignmentList = new ArrayList<>();
        adapter = new AssignmentAdapter(assignmentList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void loadAssignments() {
        db.collection("assignments")
                .orderBy("assignmentDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        assignmentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student assignment = document.toObject(Student.class);
                            assignment.setId(document.getId());
                            assignmentList.add(assignment);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error loading assignments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {
        private List<Student> assignments;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public AssignmentAdapter(List<Student> assignments) {
            this.assignments = assignments;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_assignment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Student assignment = assignments.get(position);
            holder.bind(assignment, dateFormat);
        }

        @Override
        public int getItemCount() {
            return assignments.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textStudentName, textStudentBatch, textTeacher, textCourse, textDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textStudentName = itemView.findViewById(R.id.textStudentName);
                textStudentBatch = itemView.findViewById(R.id.textStudentBatch);
                textTeacher = itemView.findViewById(R.id.textTeacher);
                textCourse = itemView.findViewById(R.id.textCourse);
                textDate = itemView.findViewById(R.id.textDate);
            }

            public void bind(Student assignment, SimpleDateFormat dateFormat) {
                textStudentName.setText(assignment.getStudentName());
                textStudentBatch.setText("Batch: " + assignment.getStudentBatch() + " | Module: " + assignment.getStudentModule());
                textTeacher.setText("Teacher: " + assignment.getTeacherName() + " (" + assignment.getTeacherSubject() + ")");
                textCourse.setText("Course: " + assignment.getCourseName());
                textDate.setText("Assigned: " + dateFormat.format(new Date(assignment.getAssignmentDate())));
            }
        }
    }
}