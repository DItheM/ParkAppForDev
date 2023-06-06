package com.example.parkapp.database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.renderscript.ScriptGroup;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.parkapp.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class Request {
    public String uid;
    public String requestId;
    public String spotId;
    public Boolean accepted = false;
    public Boolean confirmed = false;

    public Request () {

    }

    public Request (String uid, String requestId, String spotId) {
        this.uid = uid;
        this.requestId = requestId;
        this.spotId = spotId;
    }

    public Request (String uid, String requestId, String spotId, Boolean accepted, Boolean confirmed) {
        this.uid = uid;
        this.requestId = requestId;
        this.spotId = spotId;
        this.accepted = accepted;
        this.confirmed = confirmed;
    }


    public String getUid() {
        return uid;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSpotId() {
        return spotId;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }



    //process vehicle type
    public static String getVehicleType (String string) {
        int value = Integer.parseInt(string);
        String type = "Not Specified";
        if (value == 1) {
            type = "Foot Cycle";
        } else if (value == 2) {
            type = "Motor Cycle";
        } else if (value == 3) {
            type = "Car";
        } else if (value == 4) {
            type = "Van";
        } else if (value == 5) {
            type = "Lorry";
        }
        return type;
    }

    //display profile pic method
    public static void displayPFP (String pfpURL, ImageView pfp, Context context) {
        if (Objects.equals(pfpURL, "")) {
//            Glide.with(context).load("https://firebasestorage.googleapis.com/v0/b/park-app-c2769.appspot.com/o/default%2Fprofile.png?alt=media&token=793f7ac2-840b-4cbf-b0ee-9b1327e54a2f").into(pfp);
            pfp.setImageResource(R.drawable.profile);
        } else {
            Glide.with(context).load(pfpURL).into(pfp);
        }
    }

    //display charge method
    public static String processCharge (String charge) {
        if (charge.equalsIgnoreCase("free")) {
            return "Free";
        } else {
            return "LKR " + charge + " per hour";
        }
    }

    //back button functionality
    public static void onPressBack (Activity activity) {
        ImageView btn = activity.findViewById(R.id.back_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    //open image picker
    public static void openImagePicker (Fragment fragment) {
        ImagePicker.with(fragment)
                .crop(1, 1)	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    //upload image to firebase
    public static void uploadImage (Uri uri, ImageView pfp, Context context, DatabaseHelper databaseHelper, int type) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait while Updating Image...");
        progressDialog.setTitle("Update Image");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(DatabaseHelper.userId());

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                DatabaseReference mDatabase = DatabaseHelper.mDatabase();
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mDatabase.child("userData").child(DatabaseHelper.userId()).child("pfpURL").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pfp.setImageURI(uri);
                                if (type == 0) {
                                    databaseHelper.updatePFPUrlOwner(DatabaseHelper.userId(), uri.toString());
                                } else {
                                    databaseHelper.updatePFPUrlOrg(DatabaseHelper.userId(), uri.toString());
                                }
                                progressDialog.dismiss();
                                Toast.makeText(context, "Image updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
                    }
                });}
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //method to hide and show password
    public static void hidePassword (EditText editText) {
        final boolean[] isHidden = {true};
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (isHidden[0]) {
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        if(event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, R.drawable.hidden, 0);
                            editText.setTransformationMethod(null);
                            isHidden[0] = false;
                            return true;
                        }
                    }
                } else {
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        if(event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, R.drawable.ic_baseline_remove_red_eye_24, 0);
                            editText.setTransformationMethod(new PasswordTransformationMethod());
                            isHidden[0] = true;
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }
}
