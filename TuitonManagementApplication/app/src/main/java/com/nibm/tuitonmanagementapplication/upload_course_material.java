package com.nibm.tuitonmanagementapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class upload_course_material extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 100;
    private Spinner spinnerCourse;
    private EditText editMaterialTitle, editFilePath;
    private Button btnBrowseFile, btnUpload;
    private LinearLayout materialContainer;
    private CardView sampleCard;
    private Uri selectedFileUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageTask uploadTask;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_upload_materials);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Initialize views
        spinnerCourse = findViewById(R.id.spinnerCourse);
        editMaterialTitle = findViewById(R.id.editMaterialTitle);
        editFilePath = findViewById(R.id.editFilePath);
        btnBrowseFile = findViewById(R.id.btnBrowseFile);
        btnUpload = findViewById(R.id.btnUpload);
        materialContainer = findViewById(R.id.materialContainer);
        sampleCard = findViewById(R.id.sampleCard);
        sampleCard.setVisibility(View.GONE); // Keep template hidden

        // Setup course spinner
        setupSpinner();

        // Setup browse button
        btnBrowseFile.setOnClickListener(v -> openFilePicker());

        // Setup upload button
        btnUpload.setOnClickListener(v -> {
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(this, "Upload already in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadMaterial();
            }
        });

        // Load existing materials
        loadMaterials();
    }

    private void setupSpinner() {
        String[] courses = {
                "Diploma in Software Engineering",
                "Higher National Diploma in Software Engineering",
                "Diploma in Engineering",
                "Certificate in Engineering",
                "Diploma in Business Management"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courses
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                editFilePath.setText(getFileName(selectedFileUri));
            }
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex);
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    private void uploadMaterial() {
        String course = spinnerCourse.getSelectedItem().toString();
        String title = editMaterialTitle.getText().toString().trim();

        if (course.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "Please select course and enter title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading file...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get file name
        String fileName = getFileName(selectedFileUri);
        if (fileName == null || fileName.isEmpty()) {
            fileName = "material_" + System.currentTimeMillis();
        }

        // Create storage reference
        StorageReference fileRef = storage.getReference()
                .child("materials/" + fileName);

        // Create upload task
        uploadTask = fileRef.putFile(selectedFileUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading... " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveToFirestore(course, title, uri.toString());
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UPLOAD", "Upload failed: " + e.getMessage(), e);
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String course, String title, String fileUrl) {
        Map<String, Object> material = new HashMap<>();
        material.put("course", course);
        material.put("title", title);
        material.put("fileUrl", fileUrl);
        material.put("timestamp", System.currentTimeMillis());

        db.collection("materials")
                .add(material)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Material uploaded!", Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadMaterials(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Error saving document: " + e.getMessage(), e);
                    progressDialog.dismiss();
                    Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
                });
    }

    private void resetForm() {
        editMaterialTitle.setText("");
        editFilePath.setText("");
        selectedFileUri = null;
    }

    private void loadMaterials() {
        // Clear existing cards (except template)
        for (int i = materialContainer.getChildCount() - 1; i >= 0; i--) {
            View child = materialContainer.getChildAt(i);
            if (child.getTag() != null && child.getTag().equals("material_card")) {
                materialContainer.removeViewAt(i);
            }
        }

        // Get materials from Firestore
        db.collection("materials")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        String course = document.getString("course");
                        String fileUrl = document.getString("fileUrl");

                        if (title != null && course != null && fileUrl != null) {
                            addMaterialCard(
                                    document.getId(),
                                    title,
                                    course,
                                    fileUrl
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show();
                });
    }

    private void addMaterialCard(String docId, String title, String course, String fileUrl) {
        // Clone the sample card
        CardView newCard = new CardView(this);
        newCard.setLayoutParams(sampleCard.getLayoutParams());
        newCard.setCardBackgroundColor(sampleCard.getCardBackgroundColor());
        newCard.setRadius(sampleCard.getRadius());
        newCard.setCardElevation(sampleCard.getCardElevation());
        newCard.setTag("material_card");

        // Inflate inner layout
        LinearLayout innerLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.material_card_inner, null);

        // Setup views
        ImageView icon = innerLayout.findViewById(R.id.icon);
        TextView materialTitle = innerLayout.findViewById(R.id.materialTitle);
        ImageButton btnEdit = innerLayout.findViewById(R.id.btnEdit);
        ImageButton btnDelete = innerLayout.findViewById(R.id.btnDelete);

        // to open file
        newCard.setOnClickListener(v -> openMaterial(fileUrl));

        // Set file type icon
        if (fileUrl != null) {
            if (fileUrl.toLowerCase().contains(".pdf")) {
                icon.setImageResource(R.drawable.ic_pdf);
            } else {
                icon.setImageResource(R.drawable.ic_word);
            }
        }

        materialTitle.setText(title);

        // Setup delete button
        btnDelete.setOnClickListener(v -> deleteMaterial(docId, fileUrl));

        // Setup edit button (optional)
        btnEdit.setOnClickListener(v -> {
            // Implement edit functionality if needed
        });

        // Add inner layout to card
        newCard.addView(innerLayout);

        // Add card to container (after header)
        int position = findUploadedMaterialsHeaderPosition();
        if (position >= 0) {
            materialContainer.addView(newCard, position + 1);
        } else {
            materialContainer.addView(newCard); // Fallback
        }
    }

    private void openMaterial(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(this, "Invalid file URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine MIME type based on file extension
        String mimeType = "application/pdf"; // default
        if (fileUrl.toLowerCase().contains(".docx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileUrl.toLowerCase().contains(".doc")) {
            mimeType = "application/msword";
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(fileUrl), mimeType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Verify that there's an app to handle the intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // If no app is available, open in browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            Log.e("OPEN_FILE", "Error opening file", e);
            Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int findUploadedMaterialsHeaderPosition() {
        for (int i = 0; i < materialContainer.getChildCount(); i++) {
            View child = materialContainer.getChildAt(i);
            if (child instanceof TextView) {
                String text = ((TextView) child).getText().toString();
                if (text.equalsIgnoreCase("uploaded materials")) {
                    return i;
                }
            }
        }
        return -1; // Not found
    }

    private void deleteMaterial(String docId, String fileUrl) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this material?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DocumentReference docRef = db.collection("materials").document(docId);
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // Document exists - proceed with deletion
                            docRef.delete().addOnSuccessListener(aVoid -> {
                                deleteStorageFile(fileUrl); // Handle storage deletion
                                loadMaterials();
                            });
                        } else {
                            Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteStorageFile(String fileUrl) {
        if (fileUrl != null) {
            try {
                StorageReference fileRef = storage.getReferenceFromUrl(fileUrl);
                fileRef.delete().addOnFailureListener(e ->
                        Log.e("STORAGE", "Delete failed: " + e.getMessage()));
            } catch (IllegalArgumentException e) {
                Log.e("STORAGE", "Invalid URL: " + fileUrl);
            }
        }
    }
}