package com.example.parkapp;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.fragments_organizations.DashboardOrganizationsFragment;
import com.example.parkapp.fragments_organizations.MenuMainOrganizationActivity;
import com.example.parkapp.fragments_owners.DashboardOwnersFragment;
import com.example.parkapp.fragments_owners.MenuMainActivity;
import com.example.parkapp.fragments_owners.view_books.BottomNavigationViewBooksActivity;
import com.example.parkapp.signup.SignUpActivity;
import com.example.parkapp.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;


public class AuthCheckActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_check);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //delay for 1 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuth();
            }
        }, 1000);
    }

    public void checkAuth() {
        if (user != null) {
            DatabaseHelper.mDatabase().child("userData").child(user.getUid()).child("accType").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getValue().toString().equals("1")) {
                            Intent intent = new Intent(AuthCheckActivity.this, MenuMainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(AuthCheckActivity.this, MenuMainOrganizationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AuthCheckActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Intent intent = new Intent(AuthCheckActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}