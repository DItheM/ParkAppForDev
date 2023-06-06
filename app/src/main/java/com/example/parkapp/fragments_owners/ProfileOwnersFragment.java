package com.example.parkapp.fragments_owners;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.database.User;
import com.example.parkapp.databinding.FragmentProfileOwnersBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ProfileOwnersFragment extends Fragment {

    EditText name, phoneNo, licenseNo, model;
    RadioGroup radioGroup;
    RadioButton footCycle, motorCycle, car, van, lorry;
    Button saveBtn;
    DatabaseReference mDatabase;
    ProgressDialog progressDialog;
    DatabaseHelper databaseHelper;
    int vehicleType = 0;
    User user1;
    String pfpURL;
    ImageView pfp;
    TextView updatePFPLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_owners, container, false);

        name = v.findViewById(R.id.name);
        phoneNo = v.findViewById(R.id.phone_no);
        licenseNo = v.findViewById(R.id.description);
        model = v.findViewById(R.id.model_name);
        radioGroup = v.findViewById(R.id.radioGroup);
        footCycle = v.findViewById(R.id.op_footCycle);
        motorCycle = v.findViewById(R.id.op_motorCycle);
        car = v.findViewById(R.id.op_car);
        van = v.findViewById(R.id.op_van);
        lorry = v.findViewById(R.id.op_lorry);
        saveBtn = v.findViewById(R.id.save_btn);
        pfp = v.findViewById(R.id.pfp_owners);
        updatePFPLink = v.findViewById(R.id.update_pfp_link);

        progressDialog = new ProgressDialog(this.getContext());
        databaseHelper = new DatabaseHelper(this.getContext());
        mDatabase = databaseHelper.mDatabase();

        getData();
        Request.displayPFP(pfpURL, pfp, this.getContext());

        updatePFPLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request.openImagePicker(ProfileOwnersFragment.this);
            }
        });

        //click listeners for radio buttons
        footCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioBtnClicked(v);
            }
        });
        motorCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioBtnClicked(v);
            }
        });
        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioBtnClicked(v);
            }
        });
        van.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioBtnClicked(v);
            }
        });
        lorry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioBtnClicked(v);
            }
        });

        //save button click listener
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        return v;
    }

    //radio button click method
    public void onRadioBtnClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        //check which radio button was clicked
        switch (view.getId()) {
            case R.id.op_footCycle:
                if (checked)
                    vehicleType = 1;
                break;

            case R.id.op_motorCycle:
                if (checked)
                    vehicleType = 2;
                break;

            case R.id.op_car:
                if (checked)
                    vehicleType = 3;
                break;

            case R.id.op_van:
                if (checked)
                    vehicleType = 4;
                break;

            case R.id.op_lorry:
                if (checked)
                    vehicleType = 5;
                break;
        }
    }

    //get data from sqlite db
    public void getData () {
        Cursor userData = databaseHelper.getAllData();
        while (userData.moveToNext()) {
            user1 = new User(userData.getString(0), userData.getString(1), userData.getString(2));
            name.setText(userData.getString(3));
            phoneNo.setText(userData.getString(4));
            licenseNo.setText(userData.getString(5));
            model.setText(userData.getString(6));
            setChecked(Integer.parseInt(userData.getString(7)));
            pfpURL = userData.getString(8);
        }

        //after changing the pfp, it will automatically updates the link
        mDatabase.child("userData").child(DatabaseHelper.userId()).child("pfpURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    pfpURL = snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayToast(error.toString());
            }
        });
    }

    //save data to firebase db and sqlite db
    public void saveData () {
        progressDialog.setMessage("Please wait while saving...");
        progressDialog.setTitle("Save");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String userName = name.getText().toString();
        String phoneNumber = phoneNo.getText().toString();
        String licenseNumber = licenseNo.getText().toString();
        String modelName = model.getText().toString();

        User user = new User(user1.getAccType(), userName, phoneNumber, licenseNumber, modelName, String.valueOf(vehicleType), pfpURL);
        mDatabase.child("userData").child(user1.getUserId()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        databaseHelper.updateData(user1.getUserId(), user1.getEmail(), user1.getAccType(), userName, phoneNumber, licenseNumber, modelName, String.valueOf(vehicleType), pfpURL);
                        progressDialog.dismiss();
                        displayToast("Saved");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        displayToast(e.toString());
                    }
                });
    }

    //set checked radio group from db data
    public void setChecked (int value) {
        switch (value) {
            case 1:
                footCycle.setChecked(true);
                break;

            case 2:
                motorCycle.setChecked(true);
                break;

            case 3:
                car.setChecked(true);
                break;

            case 4:
                van.setChecked(true);
                break;

            case 5:
                lorry.setChecked(true);
                break;
        }
    }

    //display toast methods for unreachable inner methods
    public void displayToast (String msg) {
        Toast.makeText(this.getContext(), ""+msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Request.uploadImage(uri, pfp, getContext(), databaseHelper, 0);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(getContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}