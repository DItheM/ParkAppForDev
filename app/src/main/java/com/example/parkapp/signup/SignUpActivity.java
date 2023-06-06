package com.example.parkapp.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.database.User;
import com.example.parkapp.fragments_organizations.MenuMainOrganizationActivity;
import com.example.parkapp.fragments_owners.MenuMainActivity;
import com.example.parkapp.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    String[] item = {"Vehicle Owner", "Organization"};

    AutoCompleteTextView autoCompleteTextView;

    ArrayAdapter<String> adapterItems;

    EditText inputEmail, inputPassword, inputRePassword;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    ProgressDialog progressDialog;

    FirebaseAuth mAuth;

    FirebaseUser user;

    RadioGroup group;

    RadioButton option1, option2;

    private DatabaseReference mDatabase;

    int accType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        inputRePassword = findViewById(R.id.re_password);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        group = findViewById(R.id.radioGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        mDatabase = DatabaseHelper.mDatabase();
        Request.hidePassword(inputPassword);
        Request.hidePassword(inputRePassword);
    }

    //radio button press handle
    public void onRadioBtnClicked (View view) {
        boolean checked = ((RadioButton) view).isChecked();

        //check which radio button was clicked
        switch (view.getId()) {
            case R.id.option1:
                if (checked)
                    accType = 1;
                break;

            case R.id.option2:
                if (checked)
                    accType = 2;
                break;

        }
    }

    //sign up button pressed handle
    //input field validations
    //write data to firebase db according to selected account type
    //show error messages, toast messages and loading dialogs
    public void onSignUpPressed (View view) {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String rePassword = inputRePassword.getText().toString();

        if (!email.matches(emailPattern)) {
            inputEmail.setError("Enter Correct Email");
            inputEmail.requestFocus();
        } else if (password.isEmpty() || password.length() < 6) {
            inputPassword.setError("Password should have more than 6 characters");
            inputPassword.requestFocus();
        } else if (!password.equals(rePassword)) {
            inputRePassword.setError("Password not match in both fields");
            inputRePassword.requestFocus();
        } else if (accType == 0) {
            Toast.makeText(this, "Select your account type", Toast.LENGTH_SHORT).show();
        } else {
                progressDialog.setMessage("Please wait while registration...");
                progressDialog.setTitle("Registration");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            mDatabase.child("userData").child(user.getUid()).setValue(createUserObject())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

//                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
//                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    //create user objects according to account type
    //for write data to firebase db
    //for accType, 1 is vehicle owners, 2 is organizations
    public Object createUserObject () {
        if (accType == 1) {
            User newUser = new User(String.valueOf(accType), "", "", "", "", "0", "");
            return newUser;
        } else {
            User newUser = new User(String.valueOf(accType), "", "", "", "");
            return newUser;
        }
    }
}