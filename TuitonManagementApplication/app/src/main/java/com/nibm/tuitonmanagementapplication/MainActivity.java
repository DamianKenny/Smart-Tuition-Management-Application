package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
//import com.nibm.tuitonmanagementapplication.ReportsActivity;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView welcomeText, tvStudentCount;
    private CardView cardRegisterStudent, cardManageStudents, cardManageTeachers, cardAssignments, cardReports;
    private Button logoutButton;

    private FirebaseFirestore db;

    private String userDocumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        userDocumentId = getIntent().getStringExtra("DOCUMENT_ID");

        initializeViews();
        findViewById(R.id.menuButton).setOnClickListener(v -> toggleDrawer());
        setupHeader();
        setupClickListeners();
        loadStudentCount();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        tvStudentCount = findViewById(R.id.tvStudentCount); // Make sure this TextView exists in XML
        cardRegisterStudent = findViewById(R.id.cardRegisterStudent); // Add this card in XML
        cardManageStudents = findViewById(R.id.cardManageStudents);
        cardManageTeachers = findViewById(R.id.cardManageTeachers);
        cardAssignments = findViewById(R.id.cardAssignments);
        cardReports = findViewById(R.id.cardReports);

    }

    private void setupHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailText = headerView.findViewById(R.id.userEmail);

        // Fetch user data using document ID
        if (userDocumentId != null) {
            db.collection("users").document(userDocumentId).get()
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

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void setupClickListeners() {
        if (cardRegisterStudent != null) {
            cardRegisterStudent.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegisterStudentActivity.class);
                startActivity(intent);
            });
        }

        cardManageStudents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageStudentsActivity.class);
            startActivity(intent);
        });

        cardManageTeachers.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageTeachersActivity.class);
            startActivity(intent);
        });

        cardAssignments.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudentAssignmentActivity.class);
            startActivity(intent);
        });

//        cardReports.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, ReportsActivity.class);
//            startActivity(intent);
//        });
    }

    private void loadStudentCount() {
        db.collection("users")
                .whereEqualTo("userType", "com/nibm/tuitonmanagementapplication/Student")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = 0;
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) count = snapshot.size();
                        if (tvStudentCount != null)
                            tvStudentCount.setText("Total Students: " + count);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudentCount(); // refresh student count
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
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
