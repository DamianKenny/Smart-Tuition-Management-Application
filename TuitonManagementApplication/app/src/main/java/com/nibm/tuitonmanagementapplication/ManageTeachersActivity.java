package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class ManageTeachersActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuButton;
    private CardView cardRegisterTeacher, cardManageTeachers;
    private TextView tvTeacherCount;

    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_manager); // Match this with your XML name
        mAuth = FirebaseAuth.getInstance();


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton);
        cardRegisterTeacher = findViewById(R.id.cardRegisterTeacher);
        cardManageTeachers = findViewById(R.id.cardManageTeachers);
        tvTeacherCount = findViewById(R.id.tvStudentCount);

        db = FirebaseFirestore.getInstance();

        loadTeacherCount();
        if (getSupportActionBar() != null) getSupportActionBar().hide();


        // Open drawer on menu icon click
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));


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
                Intent intent = new Intent(ManageTeachersActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;


        });


        // Card click listeners
        cardRegisterTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterTeacherActivity.class);
            startActivity(intent);
        });

        cardManageTeachers.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherManager.class);
            startActivity(intent);
        });

        // Load total teacher count
        loadTeacherCount();
    }


    private void loadTeacherCount() {
        db.collection("users")
                .whereEqualTo("userType", "Teacher")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvTeacherCount.setText("Total Teachers: " + count);
                })
                .addOnFailureListener(e -> {
                    tvTeacherCount.setText("Total Teachers: 0");
                    Toast.makeText(this, "Failed to load teacher count", Toast.LENGTH_SHORT).show();
                });
    }


    // Handle back button to close drawer if open
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
