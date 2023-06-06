package com.example.parkapp.fragments_organizations;

import android.app.Activity;
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
import com.example.parkapp.fragments_owners.ProfileOwnersFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ProfileOrganizationsFragment extends Fragment {

    EditText name, phoneNo, description;
    TextView updatePFPLink;
    Button saveBtn;
    DatabaseReference mDatabase;
    ProgressDialog progressDialog;
    DatabaseHelper databaseHelper;
    User user1;
    String pfpURL;
    ImageView pfp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_organizations, container, false);

        name = v.findViewById(R.id.name_org);
        phoneNo = v.findViewById(R.id.phone_no_org);
        description = v.findViewById(R.id.description_org);
        saveBtn = v.findViewById(R.id.save_btn_org);
        pfp = v.findViewById(R.id.pfp);
        updatePFPLink = v.findViewById(R.id.update_pfp_link);

        updatePFPLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request.openImagePicker(ProfileOrganizationsFragment.this);
            }
        });

        progressDialog = new ProgressDialog(this.getContext());
        databaseHelper = new DatabaseHelper(this.getContext());
        mDatabase = databaseHelper.mDatabase();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        getData();
        Request.displayPFP(pfpURL, pfp, this.getContext());

        return v;
    }
    //get data from sqlite db
    public void getData () {
        Cursor userData = databaseHelper.getAllDataOrg();
        while (userData.moveToNext()) {
            user1 = new User(userData.getString(0), userData.getString(1), userData.getString(2));
            name.setText(userData.getString(3));
            phoneNo.setText(userData.getString(4));
            description.setText(userData.getString(5));
            pfpURL = userData.getString(6);
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
        String desc = description.getText().toString();

        User user = new User(user1.getAccType(), userName, phoneNumber, desc, pfpURL);
        mDatabase.child("userData").child(user1.getUserId()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        databaseHelper.updateData(user1.getUserId(), user1.getEmail(), user1.getAccType(), userName, phoneNumber, desc, pfpURL);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Request.uploadImage(uri, pfp, getContext(), databaseHelper, 1);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(getContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    //display toast methods for unreachable inner methods
    public void displayToast (String msg) {
        Toast.makeText(this.getContext(), ""+msg, Toast.LENGTH_SHORT).show();
    }
}