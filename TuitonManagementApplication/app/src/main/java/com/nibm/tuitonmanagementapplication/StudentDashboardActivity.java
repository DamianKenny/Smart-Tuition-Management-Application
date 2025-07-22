package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class StudentDashboardActivity extends AppCompatActivity {

    private QRGenerator qrGenerator;
    private ImageView qrImageView;
    private static final String TAG = "StudentDashboard";
    private FirebaseFirestore db;
    private String documentId;

    // UI elements
    private TextView tvStudentName, tvBatch, tvQRStatus;
    private ImageView ivQRCode;
    private ProgressBar progressBar;
    private Button btnGenerateQR, btnViewAttendance, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_dashboard);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Get document ID from intent with detailed error handling
        try {
            documentId = getIntent().getStringExtra("DOCUMENT_ID");
            Log.d(TAG, "Received document ID: " + documentId);

            if (documentId == null || documentId.isEmpty()) {
                handleError("Document ID is missing");
                return;
            }
        } catch (Exception e) {
            handleError("Error getting document ID: " + e.getMessage());
            return;
        }

        // Initialize views
        tvStudentName = findViewById(R.id.tv_student_name);
        tvBatch = findViewById(R.id.tv_batch);
        tvQRStatus = findViewById(R.id.tv_qr_status);
        ivQRCode = findViewById(R.id.iv_qr_code);
        progressBar = findViewById(R.id.progress_bar);
        btnGenerateQR = findViewById(R.id.btn_generate_qr);
        btnViewAttendance = findViewById(R.id.btn_view_attendance);
        btnLogout = findViewById(R.id.btn_logout);

        // Load student data
        loadStudentData();

        // Setup button listeners
        btnGenerateQR.setOnClickListener(v -> generateQRCode());
        btnViewAttendance.setOnClickListener(v -> openAttendanceActivity());
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void handleError(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, "Authentication error: " + message, Toast.LENGTH_LONG).show();

        // Navigate back to login
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void loadStudentData() {
        progressBar.setVisibility(View.VISIBLE);
        tvQRStatus.setText("Loading user data...");

        db.collection("users").document(documentId).get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Set student name
                            String fullName = document.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                tvStudentName.setText(fullName);
                            } else {
                                tvStudentName.setText("Name not set");
                            }

                            // Set batch information
                            String batch = document.getString("batch");
                            if (batch != null && !batch.isEmpty()) {
                                tvBatch.setText("Batch: " + batch);
                            } else {
                                tvBatch.setText("Batch: Not specified");
                            }

                            // Enable QR generation
                            btnGenerateQR.setVisibility(View.VISIBLE);
                            tvQRStatus.setText("Ready to generate QR code");
                        } else {
                            handleError("User document does not exist");
                        }
                    } else {
                        handleError("Database error: " + task.getException().getMessage());
                    }
                });
    }

    private void openAttendanceActivity() {
        Intent intent = new Intent(this, StudentAttendanceActivity.class);
        intent.putExtra("DOCUMENT_ID", documentId);
        startActivity(intent);
    }

    private void generateQRCode() {
        progressBar.setVisibility(View.VISIBLE);
        btnGenerateQR.setEnabled(false);
        tvQRStatus.setText("Generating QR code...");

        // Get student data to include in QR
        db.collection("users").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullName");
                        String batch = documentSnapshot.getString("batch");
                        String uniqueId = documentSnapshot.getString("qrUniqueId");

                        if (name == null) name = "Unknown";
                        if (batch == null) batch = "Unknown";
                        if (uniqueId == null) uniqueId = System.currentTimeMillis() + "";

                        // Format: ID|Name|Batch|UniqueID
                        String qrContent = documentId + "|" + name + "|" + batch + "|" + uniqueId;

                        generateQRImage(qrContent);
                    } else {
                        // Handle error
                    }
                });
    }

    private void generateQRImage(String qrContent) {
        new Thread(() -> {
            try {
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512);

                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                runOnUiThread(() -> {
                    ivQRCode.setImageBitmap(bitmap);
                    ivQRCode.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    btnGenerateQR.setEnabled(true);
                    tvQRStatus.setText("QR Code Generated");
                });

            } catch (WriterException e) {
                // Handle error
            }
        }).start();
    }

    private void logoutUser() {
        startActivity(new Intent(this, Login.class));
        finish();
    }
}