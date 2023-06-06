package com.example.parkapp.fragments_organizations;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.parkapp.R;
import com.example.parkapp.database.Request;

public class ViewVehicleOwnerProfileActivity extends AppCompatActivity {

    TextView name, licensePlate, vehicleType, vehicleModel, phoneNo;

    ImageView pfp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vehicle_owner_profile);

        name = findViewById(R.id.name);
        licensePlate = findViewById(R.id.license_no);
        vehicleType = findViewById(R.id.vehicle_type);
        vehicleModel = findViewById(R.id.model);
        phoneNo = findViewById(R.id.phone_no_org);
        pfp = findViewById(R.id.pfp);
        Request.onPressBack(this);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        name.setText(bundle.getString("name"));
        licensePlate.setText(bundle.getString("licenseNo"));
        phoneNo.setText(bundle.getString("phoneNo"));
        vehicleType.setText(bundle.getString("type"));
        vehicleModel.setText(bundle.getString("model"));
        Request.displayPFP(bundle.getString("pfp"), pfp, this);

    }
}