package com.example.parkapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.compose.runtime.snapshots.Snapshot;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkapp.database.DatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AboutFragment extends Fragment {

    ImageView linkedin, behance, github, web;
    TextView details;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        linkedin = v.findViewById(R.id.linkedin);
        behance = v.findViewById(R.id.behance);
        github = v.findViewById(R.id.github);
        web = v.findViewById(R.id.web);
        details = v.findViewById(R.id.details);

        //get about data from firebase
        DatabaseHelper.mDatabase().child("about").child("details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    details.setText(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), ""+error, Toast.LENGTH_SHORT).show();
            }
        });

        //get links from firebase
        DatabaseHelper.mDatabase().child("about").child("links").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setClickListener(snapshot.child("linkedin"), linkedin);
                setClickListener(snapshot.child("behance"), behance);
                setClickListener(snapshot.child("github"), github);
                setClickListener(snapshot.child("web"), web);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), ""+error, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    //set click listener on social icons
    private void setClickListener (DataSnapshot snapshot, ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if data available open url
                //if not show toast message
                if (snapshot.exists()) {
                    Uri webpage = Uri.parse(snapshot.getValue().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getContext(), "Not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}