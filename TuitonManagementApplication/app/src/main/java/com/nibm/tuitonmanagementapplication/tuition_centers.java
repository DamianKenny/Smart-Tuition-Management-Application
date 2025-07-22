package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class tuition_centers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.location_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // location listeners
        findViewById(R.id.colomboMap).setOnClickListener(this::goToColombo);
        findViewById(R.id.kandyMap).setOnClickListener(this::goToKandy);
        findViewById(R.id.galleMap).setOnClickListener(this::goToGalle);

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    public void goToColombo(View v) {
        Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
        Uri location = Uri.parse("geo:6.9063,79.8708?q=Shakthi+Institute+Sri+Lanka");
        Intent intent = new Intent(Intent.ACTION_VIEW, location);
        //intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    public void goToKandy(View v) {
        Uri location = Uri.parse("geo:7.2987,80.6356");
        Intent intent = new Intent(Intent.ACTION_VIEW, location);
        //intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    public void goToGalle(View v) {
        Uri location = Uri.parse("geo:6.0371,80.2239");
        Intent intent = new Intent(Intent.ACTION_VIEW, location);
        //intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }
}


