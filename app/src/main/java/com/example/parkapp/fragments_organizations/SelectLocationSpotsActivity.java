package com.example.parkapp.fragments_organizations;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
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
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.Request;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SelectLocationSpotsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int LOCATION_PERMISSION_CODE = 101;

    EditText searchBar;

    FusedLocationProviderClient fusedLocationProviderClient;

    Geocoder geocoder;

    Marker marker;

    Button confirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location_spots);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        searchBar = findViewById(R.id.searchBar);
        confirmBtn = findViewById(R.id.confirm_btn);
        Request.onPressBack(this);

        //geocoder for search location
        geocoder = new Geocoder(SelectLocationSpotsActivity.this, Locale.getDefault());

        //check permission
        if(isPermissionGranted()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_spot);
            mapFragment.getMapAsync(this);
        } else {
            requestLocationPermission();
        }


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
                                    mMap.clear();
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                                    LatLng myLocation = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                                    marker = mMap.addMarker(new MarkerOptions().position(myLocation));
                                    marker.setDraggable(true);
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

    //set click listener for confirm button
    private void setConfirmBtn () {
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectLocationSpotsActivity.this, SpotsActivity.class);
                Bundle bundle = new Bundle();
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    bundle.putString("latitude", String.valueOf(addresses.get(0).getLatitude()));
                    bundle.putString("longitude", String.valueOf(addresses.get(0).getLongitude()));
                    if (addresses.get(0).getAddressLine(0) == null) {
                        bundle.putString("location", String.valueOf(addresses.get(0).getAdminArea()));
                    } else {
                        bundle.putString("location", String.valueOf(addresses.get(0).getAddressLine(0)));
                    }

                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
                //search location function
                List<Address> searchList = null;
                String gL = String.valueOf(s);
                if (gL != null) {
                    try {
                        searchList = geocoder.getFromLocationName(gL, 1);
                        if (searchList.size() > 0) {
                            mMap.clear();
                            LatLng resultLocation = new LatLng(searchList.get(0).getLatitude(), searchList.get(0).getLongitude());
                            marker = mMap.addMarker(new MarkerOptions().position(resultLocation));
                            marker.setDraggable(true);
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

    //set marker drag listener on the map
    private void setMarkerDragListener () {
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        setSearchLocation();
        setMarkerDragListener();

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLocation();

            //set click listener for my location button on map
            //then call getLocation() again
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    getLocation();
                    return false;
                }
            });

            setConfirmBtn();
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