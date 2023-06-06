package com.example.parkapp.fragments_owners;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.MonthDisplayHelper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.BookRequest;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.databinding.ActivityBookForParkBinding;
import com.example.parkapp.databinding.ActivitySearchForParkBinding;
import com.example.parkapp.fragments_owners.view_books.BottomNavigationViewBooksActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookForParkActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ActivityBookForParkBinding binding;

    private static final int LOCATION_PERMISSION_CODE = 101;

    TextView Loc, displayDate, displayFromTime, displayToTime, displayError, displayPrice, spotName, charge, orgName, viewProfileLink;

    ImageView setDateBtn, setFromTimeBtn, setToTimeBtn, pfpOrg, closeBtn;

    EditText searchBar;

    Button requestBtn, viewRequestsBtn, bookBtn;

    FusedLocationProviderClient fusedLocationProviderClient;

    ConstraintLayout getLocation, infoSection, profileSection, setDataSection, searchSection;

    DatabaseHelper databaseHelper;
    DatabaseReference mDatabase;
    Geocoder geocoder;

    ArrayList<LatLng> addresses = new ArrayList<>();
    ArrayList<String> spotIds = new ArrayList<>();

    Marker marker;

    DataSnapshot userData;

    String date, fromTime, toTime, chargeToCal, finalPrice;

    int hourTo, minuteTo, hourFrom, minuteFrom;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBookForParkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spotName = findViewById(R.id.spot_name);
        charge = findViewById(R.id.charge);
        orgName = findViewById(R.id.name_org);
        viewProfileLink = findViewById(R.id.view_profile);
        pfpOrg = findViewById(R.id.pfp_org);
        requestBtn = findViewById(R.id.request_book_btn);
        bookBtn = findViewById(R.id.book_btn);
        searchSection = findViewById(R.id.bottomSection);
        infoSection = findViewById(R.id.infoSection);
        closeBtn = findViewById(R.id.close_btn);
        viewRequestsBtn = findViewById(R.id.view_requests_btn);


        displayDate = findViewById(R.id.display_date);
        displayFromTime = findViewById(R.id.display_from_time);
        displayToTime = findViewById(R.id.display_to_time);
        displayError = findViewById(R.id.display_error);
        displayPrice = findViewById(R.id.display_price);
        setDateBtn = findViewById(R.id.select_date_btn);
        setFromTimeBtn = findViewById(R.id.pick_from_time_btn);
        setToTimeBtn = findViewById(R.id.pick_to_time_btn);
        profileSection = findViewById(R.id.profile_org);
        setDataSection = findViewById(R.id.set_details_section);

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
                profileSection.setVisibility(View.VISIBLE);
                setDataSection.setVisibility(View.GONE);
                displayError.setVisibility(View.VISIBLE);

                toTime = null;
                fromTime = null;
                date = null;
                chargeToCal = null;


                displayToTime.setText("");
                displayFromTime.setText("");
                displayDate.setText("");
                displayPrice.setText("");
            }
        });

        //set on click listener for view requests button
        viewRequestsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookForParkActivity.this, BottomNavigationViewBooksActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("mode", "owner");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //set on click listener for book spot button
        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSection.setVisibility(View.GONE);
                profileSection.setVisibility(View.GONE);
                setDataSection.setVisibility(View.VISIBLE);
            }
        });

        //set on click listener for set date button
        setDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        //set on click listener for set from time button
        setFromTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(0);
            }
        });

        //set on click listener for set to time button
        setToTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(1);
            }
        });

        viewProfile();


        //geocoder for search location
        geocoder = new Geocoder(BookForParkActivity.this, Locale.getDefault());

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

    //open date picker dialog
    private void openDatePicker () {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    String dOM, m;
                    if (dayOfMonth < 10) {
                        dOM = "0" + dayOfMonth;
                    } else {
                        dOM = String.valueOf(dayOfMonth);
                    }
                    if (month < 10) {
                        m = "0" + month;
                    } else {
                        m = String.valueOf(month);
                    }
                    date = dOM + "." + m + "." + year;
                    displayDate.setText(date);
                    showPrice();
                }
            }, YearMonth.now().getYear(), MonthDay.now().getMonthValue(), MonthDay.now().getDayOfMonth());

            dialog.show();
        } else {
            final Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    String dOM, m;
                    if (dayOfMonth < 10) {
                        dOM = "0" + dayOfMonth;
                    } else {
                        dOM = String.valueOf(dayOfMonth);
                    }
                    if (month < 10) {
                        m = "0" + month;
                    } else {
                        m = String.valueOf(month);
                    }
                    date = dOM + "." + m + "." + year;
                    displayDate.setText(date);
                    showPrice();
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            dialog.show();
        }
    }

    //open time picker dialog
    private void openTimePicker (int mode) {
        SimpleDateFormat getHour = new SimpleDateFormat("HH", Locale.US);
        SimpleDateFormat getMinute = new SimpleDateFormat("mm", Locale.US);
        int currentHour = Integer.parseInt(getHour.format(new Date()));
        int currentMinute = Integer.parseInt(getMinute.format(new Date()));
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hOD, m;
                if (hourOfDay < 10) {
                    hOD = "0" + hourOfDay;
                } else {
                    hOD = String.valueOf(hourOfDay);
                }
                if (minute < 10) {
                    m = "0" + minute;
                } else {
                    m = String.valueOf(minute);
                }
                if (mode == 0) {
                    fromTime = hOD + " : " + m;
                    displayFromTime.setText(fromTime);
                    hourFrom = hourOfDay;
                    minuteFrom = minute;
                } else {
                    toTime = hOD + " : " + m;
                    displayToTime.setText(toTime);
                    hourTo = hourOfDay;
                    minuteTo = minute;
                }
                showPrice();
            }
        }, currentHour, currentMinute, true);

        dialog.show();
    }

    //show price method
    private void showPrice () {
        if (date != null && toTime != null && fromTime != null ) {
            if (chargeToCal == null) {
                Toast.makeText(this, "Error getting pricing info", Toast.LENGTH_SHORT).show();
            } else {
                displayError.setVisibility(View.GONE);
                if (Request.processCharge(chargeToCal).equals("Free")) {
                    finalPrice = "Free";
                    displayPrice.setText("Free");
                } else {
                    finalPrice = String.valueOf(priceCal());
                    displayPrice.setText("LKR "+ finalPrice);
                }
            }
        }
    }

    //calculate price
    private int priceCal () {
        int times = 0;
        if (hourFrom > hourTo) {
            for (int i = hourFrom; i != hourTo; i++) {
                if (i == 24) {
                    i = 0;
                }
                times++;
            }
            times--;
        }
//        if (hourFrom == hourTo) {
//            times = 24;
//        }
        if (hourFrom < hourTo) {
            times = hourTo - hourFrom;
        }

        int mDef = minuteFrom - minuteTo;
        if (minuteFrom < minuteTo) {
            times++;
        }
        if (minuteFrom > minuteTo ) {
            if (times == 0) {
                times = 24;
            }
        }

        return Integer.parseInt(chargeToCal) * times;
    }

    //set on click listener for view profile button button
    private void viewProfile () {
        viewProfileLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userData.exists()) {
                    Intent intent = new Intent(BookForParkActivity.this, ViewOrgProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("orgName", userData.child("username").getValue().toString());
                    bundle.putString("description", userData.child("description").getValue().toString());
                    bundle.putString("pfpURL", userData.child("pfpURL").getValue().toString());
                    bundle.putString("phoneNo", userData.child("phoneNo").getValue().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(BookForParkActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
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
                if (userData.exists() && date != null && toTime != null && fromTime != null) {
                    mDatabase.child("Owners").child(DatabaseHelper.userId()).child("requestedSpots").child("book").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            //check if spot is already requested
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.getValue().toString().equals(spotId)) {
                                        isRequested[0] = true;
                                        Toast.makeText(BookForParkActivity.this, "Already requested", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                            }

                            //if not requested
                            if (!isRequested[0]) {
                                mDatabase.child("Organizations").child("data").child(uid).child("counts").child("bookRequests").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                    @Override
                                    public void onSuccess(DataSnapshot dataSnapshot) {
                                        int value;
                                        if (dataSnapshot.exists()) {
                                            value = Integer.parseInt(dataSnapshot.getValue().toString()) + 1;
                                        } else {
                                            value = 1;
                                        }
                                        mDatabase.child("Organizations").child("data").child(uid).child("counts").child("bookRequests").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                String requestKey = mDatabase.child("Organizations").child("data").child(uid).child("book").child("requests").push().getKey();
                                                if (requestKey != null) {
                                                    BookRequest requestForOrg = new BookRequest(DatabaseHelper.userId(), requestKey, spotId, date, fromTime, toTime, false, finalPrice);
                                                    BookRequest requestForOwner= new BookRequest(uid, requestKey, spotId, date, fromTime, toTime, false, finalPrice);
                                                    mDatabase.child("Organizations").child("data").child(uid).child("book").child("requests").child(requestKey).setValue(requestForOrg).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mDatabase.child("Owners").child(DatabaseHelper.userId()).child("userRequests").child("book").child("requests").child(requestKey).setValue(requestForOwner).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    mDatabase.child("Owners").child(DatabaseHelper.userId()).child("requestedSpots").child("book").child(requestKey).setValue(spotId).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Toast.makeText(BookForParkActivity.this, "Requested", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(BookForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(BookForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(BookForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(BookForParkActivity.this, "Request key is null", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(BookForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(BookForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BookForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(BookForParkActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(BookForParkActivity.this, "Parking spot is not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BookForParkActivity.this, ""+e, Toast.LENGTH_SHORT).show();
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
                infoSection.setVisibility(View.GONE);
                profileSection.setVisibility(View.VISIBLE);
                setDataSection.setVisibility(View.GONE);
                displayError.setVisibility(View.VISIBLE);

                toTime = null;
                fromTime = null;
                date = null;
                chargeToCal = null;

                displayToTime.setText("");
                displayFromTime.setText("");
                displayDate.setText("");
                displayPrice.setText("");
                if (marker.getTitle() != null) {
                    mDatabase.child("allSpots").child(marker.getTitle()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                searchSection.setVisibility(View.GONE);
                                infoSection.setVisibility(View.VISIBLE);

                                spotName.setText(snapshot.child("name").getValue().toString());
                                charge.setText(Request.processCharge(snapshot.child("charge").getValue().toString()));
                                chargeToCal = snapshot.child("charge").getValue().toString();

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
                                                    Toast.makeText(BookForParkActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(BookForParkActivity.this, "error", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(BookForParkActivity.this, "Parking spot is not available", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(BookForParkActivity.this, "error", Toast.LENGTH_SHORT).show();
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