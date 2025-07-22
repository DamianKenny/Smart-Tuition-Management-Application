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
import com.nibm.tuitonmanagementapplication.model.Teacher;

import java.util.ArrayList;
import java.util.List;

public class TeacherManager extends AppCompatActivity implements
        TeacherAdapter.OnTeacherClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoTeachers;
    private EditText etSearch;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuButton;

    private List<Teacher> teacherList = new ArrayList<>();
    private List<Teacher> filteredList = new ArrayList<>();
    private TeacherAdapter adapter;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_teachers); // Your layout file

        // Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvNoTeachers = findViewById(R.id.tvNoStudents); // Label for "No teachers"
        etSearch = findViewById(R.id.etSearch);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton);

        // Setup Navigation Drawer
        navigationView.setNavigationItemSelectedListener(this);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Update UI Text
        tvNoTeachers.setText("No teachers found");

        // Setup RecyclerView
        adapter = new TeacherAdapter(filteredList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load teachers
        loadTeachers();

        // Hide ActionBar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTeachers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onTeacherClick(Teacher teacher) {
        Intent intent = new Intent(this, EditTeacherActivity.class);
        intent.putExtra("teacher", teacher);
        startActivity(intent);
    }

    private void loadTeachers() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoTeachers.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("userType", "Teacher")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        teacherList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Teacher teacher = new Teacher();
                            teacher.setId(document.getId());
                            teacher.setFullName(document.getString("fullName"));
                            teacher.setEmail(document.getString("email"));
                            teacher.setPhone(document.getString("phone"));
                            teacher.setSubject(document.getString("subject"));
                            teacher.setQualification(document.getString("qualification"));
                            teacher.setExperience(document.getString("experience"));
                            teacherList.add(teacher);
                        }

                        filteredList.clear();
                        filteredList.addAll(teacherList);
                        adapter.notifyDataSetChanged();

                        tvNoTeachers.setVisibility(teacherList.isEmpty() ? View.VISIBLE : View.GONE);

                    } else {
                        tvNoTeachers.setVisibility(View.VISIBLE);
                        tvNoTeachers.setText("Error loading teachers");
                    }
                });
    }

    private void filterTeachers(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(teacherList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Teacher teacher : teacherList) {
                if (teacher.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                        teacher.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                        (teacher.getSubject() != null && teacher.getSubject().toLowerCase().contains(lowerCaseQuery)) ||
                        (teacher.getQualification() != null && teacher.getQualification().toLowerCase().contains(lowerCaseQuery)) ||
                        (teacher.getExperience() != null && teacher.getExperience().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(teacher);
                }
            }
        }

        adapter.notifyDataSetChanged();
        tvNoTeachers.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        tvNoTeachers.setText(filteredList.isEmpty() ? "No teachers found" : "");
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
            Intent intent = new Intent(TeacherManager.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
