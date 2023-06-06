package com.example.parkapp.ui.login;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.fragments_organizations.MenuMainOrganizationActivity;
import com.example.parkapp.fragments_owners.MenuMainActivity;
import com.example.parkapp.signup.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;


public class LoginActivity extends AppCompatActivity {

    EditText inputEmail, inputPassword;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    FirebaseAuth mAuth;

    FirebaseUser user;

    ProgressDialog progressDialog;

    DatabaseHelper databaseHelper;

    DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        databaseHelper = new DatabaseHelper(this);
        Request.hidePassword(inputPassword);
    }

    //handle onCreateAccountPressed
    public void onCreateAccountPressed (View view) {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    //sign in functionality
    //input field validations
    //get data from firebase db
    //set data on sqlite db according to account type
    //display toast messages and loading dialogs
    public void onSignInPressed (View view) {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (!email.matches(emailPattern)) {
            inputEmail.setError("Enter Correct Email");
            inputEmail.requestFocus();
        } else if (password.isEmpty() || password.length() < 6) {
            inputPassword.setError("Password should have more than 6 characters");
            inputPassword.requestFocus();
        } else {
            progressDialog.setMessage("Please wait while Login...");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            databaseHelper.deleteAllData();
                            databaseHelper.deleteAllDataOrg();
                            if (task.isSuccessful()) {
                                mDatabase = databaseHelper.mDatabase();
                                mDatabase.child("userData").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                if (getValue(task, "accType").equals("1")) {
                                                    if (insertLocalDB(getValue(task, "accType"), getValue(task, "username"), getValue(task, "phoneNo"), getValue(task, "licenseNo"), getValue(task, "model"), getValue(task, "vehicleType"), getValue(task, "pfpURL"))) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(LoginActivity.this, MenuMainActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(LoginActivity.this, "Error SQLite: can't write data", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    if (insertLocalDB(getValue(task, "accType"), getValue(task, "username"), getValue(task, "phoneNo"), getValue(task, "description"), getValue(task, "pfpURL"))) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(LoginActivity.this, MenuMainOrganizationActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(LoginActivity.this, "Error SQLite: can't write data", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                        } else {
                                            // If data retrieval fails, display a message to the user.
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
            });
        }
    }

    //inset data to sqlite db method for vehicle owners
    public boolean insertLocalDB (String accType, String name, String phoneNo, String licenseNo, String model, String vehicleType,  String pfpURL) {
        boolean isAdded = databaseHelper.insertData(user.getUid(), user.getEmail(), accType, name, phoneNo, licenseNo, model, vehicleType, pfpURL);
        return isAdded;
    }

    //inset data to sqlite db method for organizations
    public boolean insertLocalDB (String accType, String name, String phoneNo, String description,  String pfpURL) {
        boolean isAdded = databaseHelper.insertData(user.getUid(), user.getEmail(), accType, name, phoneNo, description, pfpURL);
        return isAdded;
    }

    //get values from firebase db snapshot
    public String getValue (Task<DataSnapshot> task, String name) {
        return task.getResult().child(name).getValue().toString();
    }
}