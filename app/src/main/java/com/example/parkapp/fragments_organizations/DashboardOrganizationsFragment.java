package com.example.parkapp.fragments_organizations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.fragments_organizations.live_spots.BottomNavigationLiveSpotsActivity;
import com.example.parkapp.fragments_owners.BookForParkActivity;
import com.example.parkapp.fragments_owners.view_books.BottomNavigationViewBooksActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DashboardOrganizationsFragment extends Fragment {

    Button viewSpotsBtn, viewLiveSpotsBtn, ViewBookingBtn;

    DatabaseReference mDatabase;

    TextView parkRequests, onPark, bookRequests, confirmed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dashboard_organizations, container, false);

        viewSpotsBtn = v.findViewById(R.id.view_spots_button);
        mDatabase = DatabaseHelper.mDatabase();
        parkRequests = v.findViewById(R.id.park_requests);
        onPark = v.findViewById(R.id.on_park);
        bookRequests = v.findViewById(R.id.book_requests);
        confirmed = v.findViewById(R.id.confirmed);
        viewLiveSpotsBtn = v.findViewById(R.id.view_live_spots_btn);
        ViewBookingBtn = v.findViewById(R.id.view_bookings_btn);

        getData(this.getContext());

        //set on click listener for edit spots button
        viewSpotsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewSpotsActivity.class);
                startActivity(intent);
            }
        });

        //set on click listener for view live spots button
        viewLiveSpotsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BottomNavigationLiveSpotsActivity.class);
                startActivity(intent);
            }
        });

        //set on click listener for view bookings button
        ViewBookingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BottomNavigationViewBooksActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("mode", "org");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return v;
    }

    //get data from firebase and display data
    public void getData (Context context) {
        mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("spotRequests").exists()) {
                    parkRequests.setText(snapshot.child("spotRequests").getValue().toString());
                } else {
                    parkRequests.setText("0");
                }

                if (snapshot.child("onPark").exists()) {
                    onPark.setText(snapshot.child("onPark").getValue().toString());
                } else {
                    onPark.setText("0");
                }

                if (snapshot.child("bookRequests").exists()) {
                    bookRequests.setText(snapshot.child("bookRequests").getValue().toString());
                } else {
                    bookRequests.setText("0");
                }

                if (snapshot.child("bookConfirmed").exists()) {
                    confirmed.setText(snapshot.child("bookConfirmed").getValue().toString());
                } else {
                    confirmed.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, ""+error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}