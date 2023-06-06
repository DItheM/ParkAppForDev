package com.example.parkapp.fragments_organizations.live_spots;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.recycler_view.RecycleViewAdapterParkRequests;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ParkConfirmedFragment extends Fragment {

    RecycleViewAdapterParkRequests adapter;

    RecyclerView recyclerView;

    DatabaseReference mDatabase;

    ArrayList<Request> list;

    TextView message;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_park_confirmed, container, false);

        message = v.findViewById(R.id.message);
        mDatabase = DatabaseHelper.mDatabase();

        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();

        //sett database changes listener
        //when changing recycler view will updates
        //if no data available, a message will be shown
        if (DatabaseHelper.userId() != null) {
            mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("park").child("confirmed").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    message.setVisibility(View.GONE);
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Request request = dataSnapshot.getValue(Request.class);
                        list.add(request);
                    }
                    adapter.notifyDataSetChanged();

                    if (!snapshot.exists()) {
                        message.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "" + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "null user id", Toast.LENGTH_SHORT).show();
        }

        adapter = new RecycleViewAdapterParkRequests(getContext(), list, 2);
        recyclerView.setAdapter(adapter);

        return v;
    }
}