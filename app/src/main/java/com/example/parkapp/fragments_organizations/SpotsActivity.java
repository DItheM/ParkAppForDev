package com.example.parkapp.fragments_organizations;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.database.Spot;
import com.example.parkapp.databinding.ActivityBookForParkBinding;
import com.example.parkapp.fragments_owners.SearchForParkActivity;
import com.example.parkapp.signup.SignUpActivity;
import com.example.parkapp.ui.login.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SpotsActivity extends AppCompatActivity {

    EditText spotName, charges;

    TextView displayLocation;

    Button addBtn, chooseLocationBtn;

    String location;

    String latitude;

    String longitude;

    Bundle bundle;

    DatabaseReference mDatabase;

    FirebaseUser user;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spots);

        displayLocation = findViewById(R.id.selected_location);
        spotName = findViewById(R.id.spot_name);
        charges = findViewById(R.id.charge);
        mDatabase = DatabaseHelper.mDatabase();
        progressDialog = new ProgressDialog(this);
        Request.onPressBack(this);

        //get addBtn and set click listener
        //set data in firebase
        addBtn = findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = spotName.getText().toString();
                String charge = charges.getText().toString();
                if (bundle == null) {
                    Toast.makeText(SpotsActivity.this, "Choose your spot location", Toast.LENGTH_SHORT).show();
                } else if (name.isEmpty()) {
                    spotName.setError("Empty");
                } else  if (charge.isEmpty()) {
                    charges.setError("Empty");
                } else {
                    progressDialog.setMessage("Please wait while creating spot...");
                    progressDialog.setTitle("Creating Spot");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    user = FirebaseAuth.getInstance().getCurrentUser();
                    Spot spot = new Spot(name, charge, latitude, longitude);
                    Spot spotWithUid = new Spot(user.getUid(), name, charge, latitude, longitude);
                    String key = mDatabase.child("Organizations").child("spots").child(user.getUid()).push().getKey();
                    if (key != null) {
                        mDatabase.child("Organizations").child("spots").child(user.getUid()).child(key).setValue(spot)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        mDatabase.child("allSpots").child(key).setValue(spotWithUid)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(SpotsActivity.this, "Spot created", Toast.LENGTH_SHORT).show();

//                                                        Intent intent = new Intent(SpotsActivity.this, ViewSpotsActivity.class);
//                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(SpotsActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(SpotsActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SpotsActivity.this, "Error - pot key is null", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //get chooseLocationBtn and set click listener
        chooseLocationBtn = findViewById(R.id.choose_location_btn);
        chooseLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotsActivity.this, SelectLocationSpotsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getSelectedLocation();
    }

    private void getSelectedLocation () {
        //Get the bundle
        bundle = getIntent().getExtras();

        if (bundle != null) {
            //Extract the data…
            location = bundle.getString("location");
            latitude = bundle.getString("latitude");
            longitude = bundle.getString("longitude");
            displayLocation.setText(location);
            displayLocation.setVisibility(View.VISIBLE);
        } else {
            displayLocation.setVisibility(View.GONE);
        }
    }

}