package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.tuitonmanagementapplication.model.Student;

public class Student_Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth mAuth;
    private String documentId;

    private NavigationView navigationView;

    private FirebaseFirestore db;

    private TextView welcomeText;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_student);
        mAuth = FirebaseAuth.getInstance();

        // Get document ID from intent
        documentId = getIntent().getStringExtra("DOCUMENT_ID");

        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "Document ID missing, please login again", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize all CardViews
        CardView cardAttendance = findViewById(R.id.cardViewAttendance);
        CardView cardAssignments = findViewById(R.id.cardViewAssignments);
        CardView cardSubmitAssignments = findViewById(R.id.cardSubmitAssignments);
        CardView cardMaterials = findViewById(R.id.cardMaterials);
        CardView cardResults = findViewById(R.id.cardViewResults);
        CardView cardNotifications = findViewById(R.id.cardNotifications);

        // Set click listeners with navigation stubs
        cardAttendance.setOnClickListener(v -> openAttendance());
        cardAssignments.setOnClickListener(v -> openAssignments());
        cardSubmitAssignments.setOnClickListener(v -> openSubmitAssignments());
        cardMaterials.setOnClickListener(v -> openMaterials());
        cardResults.setOnClickListener(v -> openResults());
        cardNotifications.setOnClickListener(v -> openNotifications());

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        CardView cardLocations = findViewById(R.id.cardViewLocations);
        cardLocations.setOnClickListener(v -> openTuitionCenters());

        setupHeader();

    }

    private void openTuitionCenters() {
        try {
            Intent intent = new Intent(Student_Dashboard.this, tuition_centers.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "Failed to open Tuition Centers", e);
            Toast.makeText(this, "Failed to open Tuition Centers", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailText = headerView.findViewById(R.id.userEmail);

        // Fetch user data using document ID
        if (documentId != null) {
            db.collection("users").document(documentId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                userEmailText.setText(fullName);
                                welcomeText.setText("Welcome, " + fullName + "!"); // Update welcome text
                            } else {
                                userEmailText.setText("No Name Found");
                            }
                        }
                    })
                    .addOnFailureListener(e -> userEmailText.setText("Error Loading Name"));
        } else {
            userEmailText.setText("No User Logged In");
        }

        // Close drawer button
        View closeButton = headerView.findViewById(R.id.closeButton);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }
    }


    private void openAttendance() {
        /*
        Intent intent = new Intent(this, AttendanceActivity.class);
        startActivity(intent);
        */
        showToast("Attendance feature would open here");
    }

    private void openAssignments() {
        try {
            Intent intent = new Intent(Student_Dashboard.this, Student_assignment_view.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "Failed to open assignments", e);
            Toast.makeText(this, "Failed to open assignments", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSubmitAssignments() {
        try {
            Intent intent = new Intent(Student_Dashboard.this, Student_assignment_upload.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "Failed to open assignments", e);
            Toast.makeText(this, "Failed to open assignments", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMaterials() {
        try {
            Intent intent = new Intent(Student_Dashboard.this, CourseMaterialsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "Failed to open assignments", e);
            Toast.makeText(this, "Failed to open assignments", Toast.LENGTH_SHORT).show();
        }
    }

    private void openResults() {
        try {
            Intent intent = new Intent(Student_Dashboard.this, Student_result_view.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "Failed to open assignments", e);
            Toast.makeText(this, "Failed to open assignments", Toast.LENGTH_SHORT).show();
        }
    }

    private void openNotifications() {
        try {
            if (documentId == null || documentId.isEmpty()) {
                throw new Exception("Document ID not found");
            }

            Intent intent = new Intent(Student_Dashboard.this, StudentDashboardActivity.class);
            intent.putExtra("DOCUMENT_ID", documentId);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("Navigation", "Failed to open notifications", e);
            Toast.makeText(this, "Failed to open notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(Student_Dashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}