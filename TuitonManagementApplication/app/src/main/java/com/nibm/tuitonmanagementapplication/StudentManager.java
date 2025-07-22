package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class StudentManager extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoStudents;
    private EditText etSearch;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuButton;

    // Data
    private List<Student> studentList = new ArrayList<>();
    private List<Student> filteredList = new ArrayList<>();
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvNoStudents = findViewById(R.id.tvNoStudents);
        etSearch = findViewById(R.id.etSearch);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Drawer setup
        navigationView.setNavigationItemSelectedListener(this);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // RecyclerView setup
        adapter = new StudentAdapter(filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load data
        loadStudents();

        // Search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadStudents() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoStudents.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("userType", "Student")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        studentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student student = document.toObject(Student.class);
                            student.setId(document.getId());
                            studentList.add(student);
                        }

                        filteredList.clear();
                        filteredList.addAll(studentList);
                        adapter.notifyDataSetChanged();

                        if (studentList.isEmpty()) {
                            tvNoStudents.setVisibility(View.VISIBLE);
                        } else {
                            tvNoStudents.setVisibility(View.GONE);
                        }
                    } else {
                        tvNoStudents.setVisibility(View.VISIBLE);
                        tvNoStudents.setText("Error loading students");
                    }
                });
    }

    private void filterStudents(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(studentList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Student student : studentList) {
                if (student.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                        student.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        (student.getModule() != null && student.getModule().toLowerCase().contains(lowerCaseQuery)) ||
                        (student.getBatch() != null && student.getBatch().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(student);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            tvNoStudents.setVisibility(View.VISIBLE);
            tvNoStudents.setText("No students found");
        } else {
            tvNoStudents.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            Intent intent = new Intent(StudentManager.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
