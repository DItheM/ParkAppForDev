package com.example.parkapp.fragments_owners;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.databinding.ActivitySearchForParkBinding;
import com.example.parkapp.fragments_organizations.SpotsActivity;
import com.example.parkapp.fragments_organizations.ViewSpotsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SearchForParkActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ActivitySearchForParkBinding binding;

    private static final int LOCATION_PERMISSION_CODE = 101;

    TextView Loc, spotName, charge, orgName, viewProfileLink;

    ImageView pfpOrg, closeBtn;

    Button requestBtn, viewRequestsBtn;

    EditText searchBar;

    FusedLocationProviderClient fusedLocationProviderClient;

    ConstraintLayout getLocation, searchSection, infoSection;

    DatabaseHelper databaseHelper;
    DatabaseReference mDatabase;
    Geocoder geocoder;

    ArrayList<LatLng> addresses = new ArrayList<>();
    ArrayList<String> spotIds = new ArrayList<>();

    Marker marker;

    DataSnapshot userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchForParkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spotName = findViewById(R.id.spot_name);
        charge = findViewById(R.id.charge);
        orgName = findViewById(R.id.name_org);
        viewProfileLink = findViewById(R.id.view_profile);
        pfpOrg = findViewById(R.id.pfp_org);
        requestBtn = findViewById(R.id.request_btn);
        searchSection = findViewById(R.id.bottomSection);
        infoSection = findViewById(R.id.infoSection);
        closeBtn = findViewById(R.id.close_btn);
        viewRequestsBtn = findViewById(R.id.view_requests_btn);

        Request.onPressBack(this);

        //get Mylocation Button
        getLocation = findViewById(R.id.myLocation);

        //search bar
        searchBar = findViewById(R.id.searchBar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseHelper = new DatabaseHelper(this);
        mDatabase = databaseHelper.mDatabase();

        //set on click listener for close button
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSection.setVisibility(View.VISIBLE);
                infoSection.setVisibility(View.GONE);
            }
        });

        //set on click listener for view requests button
        viewRequestsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchForParkActivity.this, ViewRequestsActivity.class);
                startActivity(intent);
            }
        });

        viewProfile();


        //geocoder for search location
        geocoder = new Geocoder(SearchForParkActivity.this, Locale.getDefault());

        //check permission
        if(isPermissionGranted()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            requestLocationPermission();
        }
    }

    //set on click listener for view profile button button
    private void viewProfile () {
        viewProfileLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userData.exists()) {
                    Intent intent = new Intent(SearchForParkActivity.this, ViewOrgProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("orgName", userData.child("username").getValue().toString());
                    bundle.putString("description", userData.child("description").getValue().toString());
                    bundle.putString("pfpURL", userData.child("pfpURL").getValue().toString());
                    bundle.putString("phoneNo", userData.child("phoneNo").getValue().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(SearchForParkActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //set on click listener for request button
    private void request (String uid, String spotId) {
        final Boolean[] isRequested = {false};
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userData.exists()) {
                    mDatabase.child("Owners").child(DatabaseHelper.userId()).child("requestedSpots").child("park").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            //check if spot is already requested
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.getValue().toString().equals(spotId)) {
                                        isRequested[0] = true;
                                        Toast.makeText(SearchForParkActivity.this, "Already requested", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                            }

                            //if not requested
                            if (!isRequested[0]) {
                                mDatabase.child("Organizations").child("data").child(uid).child("counts").child("spotRequests").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                    @Override
                                    public void onSuccess(DataSnapshot dataSnapshot) {
                                        int value;
                                        if (dataSnapshot.exists()) {
                                            value = Integer.parseInt(dataSnapshot.getValue().toString()) + 1;
                                        } else {
                                            value = 1;
                                        }
                                        mDatabase.child("Organizations").child("data").child(uid).child("counts").child("spotRequests").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                String requestKey = mDatabase.child("Organizations").child("data").child(uid).child("park").child("requests").push().getKey();
                                                if (requestKey != null) {
                                                    Request requestForOrg = new Request(DatabaseHelper.userId(), requestKey, spotId);
                                                    Request requestForOwner= new Request(uid, requestKey, spotId);
                                                    mDatabase.child("Organizations").child("data").child(uid).child("park").child("requests").child(requestKey).setValue(requestForOrg).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mDatabase.child("Owners").child(DatabaseHelper.userId()).child("userRequests").child("park").child(requestKey).setValue(requestForOwner).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    mDatabase.child("Owners").child(DatabaseHelper.userId()).child("requestedSpots").child("park").child(requestKey).setValue(spotId).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Toast.makeText(SearchForParkActivity.this, "Requested", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(SearchForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(SearchForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(SearchForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(SearchForParkActivity.this, "Request key is null", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SearchForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SearchForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SearchForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(SearchForParkActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //get MyLocation and make camera move towards my location
    private void getLocation () {
        if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
//                                Geocoder geocoder = new Geocoder(SearchForParkActivity.this, Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    Loc = findViewById(R.id.textView11);
                                    Loc.setText(addresses.get(0).getAddressLine(0));

                                    LatLng myLocation = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
//                                    mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in Sydney"));
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12.0f));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    //get parking spots data
    private void getParkingSpots () {
        //get spots data from firebase db
        mDatabase.child("allSpots").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(DataSnapshot spotsSnapshot: dataSnapshot.getChildren()) {
                        LatLng resultLocation = new LatLng(Double.parseDouble(spotsSnapshot.child("latitude")
                                .getValue().toString()), Double.parseDouble(spotsSnapshot.child("longitude").getValue().toString()));
                        addresses.add(resultLocation);
                        spotIds.add(spotsSnapshot.getKey());
                        marker = mMap.addMarker(new MarkerOptions().position(resultLocation).title(spotsSnapshot.getKey()));
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        marker.hideInfoWindow();
                    }
                } else {
                    Toast.makeText(SearchForParkActivity.this, "Parking spot is not available", Toast.LENGTH_SHORT).show();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SearchForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //set marker data
    private void setMarkers () {
        for(int pos = 0; pos < addresses.size(); pos++) {
            marker = mMap.addMarker(new MarkerOptions().position(addresses.get(pos)).title(spotIds.get(pos)));
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }
    }

    //set click listener for markers in the map
    //then get data from firebase db
    private void setMarkerClickListener (Context context) {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (marker.getTitle() != null) {
                    mDatabase.child("allSpots").child(marker.getTitle()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                searchSection.setVisibility(View.GONE);
                                infoSection.setVisibility(View.VISIBLE);

                                spotName.setText(snapshot.child("name").getValue().toString());
                                charge.setText(Request.processCharge(snapshot.child("charge").getValue().toString()));

                                mDatabase.child("userData").child(snapshot.child("uid").getValue().toString())
                                        .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            userData = snapshot;
                                            orgName.setText(snapshot.child("username").getValue().toString());
                                            Request.displayPFP(snapshot.child("pfpURL").getValue().toString(), pfpOrg, context);
                                            request(snapshot.getKey(), marker.getTitle());
                                        } else {
                                            Toast.makeText(SearchForParkActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(SearchForParkActivity.this, "error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(SearchForParkActivity.this, "Parking spot is not available", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SearchForParkActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    searchSection.setVisibility(View.VISIBLE);
                    infoSection.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    //search location functionality
    private void setSearchLocation () {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //everytime search bar input changes, location will be searched
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //if search bar is not empty, my location will hide
                if (s.length() == 0) {
                    getLocation.setVisibility(View.VISIBLE);
                } else {
                    getLocation.setVisibility(View.GONE);
                }

                //search location function
                List<Address> searchList = null;
                String gL = String.valueOf(s);
                if (gL != null) {
                    try {
                        searchList = geocoder.getFromLocationName(gL, 1);
                        if (searchList.size() > 0) {
                            mMap.clear();
                            setMarkers();
                            LatLng resultLocation = new LatLng(searchList.get(0).getLatitude(), searchList.get(0).getLongitude());
                            mMap.addMarker(new MarkerOptions().position(resultLocation));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(resultLocation, 12.0f));
//                            Log.i("SEARCH_LOCATION", gL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        setSearchLocation();
        getParkingSpots();
        setMarkerClickListener(this);

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLocation();
            getLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getLocation();
                }
            });
        }
    }

    //check permission status
    public boolean isPermissionGranted () {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    //request permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }
}