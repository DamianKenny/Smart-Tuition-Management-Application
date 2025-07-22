package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.tuitonmanagementapplication.model.Material;

import java.util.ArrayList;
import java.util.List;

public class CourseMaterialsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "CourseMaterials";
    private Spinner spinnerCourse;
    private RecyclerView recyclerViewMaterials;
    private MaterialAdapter adapter;
    private List<Material> materialList = new ArrayList<>();
    private FirebaseFirestore db;

    private NavigationView navigationView;

    private DrawerLayout drawerLayout;

    private final String[] courses = {
            "Diploma in Software Engineering",
            "Higher National Diploma in Software Engineering",
            "Diploma in Engineering",
            "Certificate in Engineering",
            "Diploma in Business Management"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_materials);

        db = FirebaseFirestore.getInstance();

        spinnerCourse = findViewById(R.id.spinnerCourse);
        recyclerViewMaterials = findViewById(R.id.recyclerViewMaterials);

        // Setup RecyclerView - pass context to adapter
        recyclerViewMaterials.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialAdapter(materialList, this);
        recyclerViewMaterials.setAdapter(adapter);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup course spinner
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courses
        );
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = parent.getItemAtPosition(position).toString();
                loadMaterials(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Load materials for the first course by default
        if (courses.length > 0) {
            loadMaterials(courses[0]);
        }

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    private void loadMaterials(String course) {
        db.collection("materials")
                .whereEqualTo("course", course)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        materialList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Material material = new Material();
                                material.setId(document.getId());
                                material.setTitle(document.getString("title"));
                                material.setCourse(document.getString("course"));
                                material.setModule(document.getString("module"));
                                material.setUploadedDate(document.getString("uploadedDate"));
                                material.setSubmissionDate(document.getString("submissionDate"));
                                material.setFileUrl(document.getString("fileUrl"));

                                materialList.add(material);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document", e);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (materialList.isEmpty()) {
                            Toast.makeText(CourseMaterialsActivity.this,
                                    "No materials found for this course", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting materials.", task.getException());
                        Toast.makeText(CourseMaterialsActivity.this,
                                "Failed to load materials.", Toast.LENGTH_SHORT).show();
                    }
                });
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
            // Updated logout without FirebaseAuth
            Intent intent = new Intent(CourseMaterialsActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}