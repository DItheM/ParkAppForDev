package com.example.parkapp.fragments_owners;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.parkapp.R;
import com.example.parkapp.database.Request;

public class ViewOrgProfileActivity extends AppCompatActivity {

    TextView orgName;

    TextView description, phoneNo;

    ImageView pfpOrg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_org_profile);

        orgName = findViewById(R.id.org_name);
        description = findViewById(R.id.description_org);
        phoneNo = findViewById(R.id.phone_no_org);
        pfpOrg = findViewById(R.id.pfp);
        Request.onPressBack(this);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        orgName.setText(bundle.getString("orgName"));
        description.setText(bundle.getString("description"));
        phoneNo.setText(bundle.getString("phoneNo"));
        Request.displayPFP(bundle.getString("pfpURL"), pfpOrg, this);

    }

}