package com.example.parkapp.fragments_organizations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.database.Spot;
import com.example.parkapp.recycler_view.RecycleViewAdapterSpots;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewSpotsActivity extends AppCompatActivity {

    Button editSpotsBtn;

    RecycleViewAdapterSpots adapter;

    RecyclerView recyclerView;

    DatabaseReference mDatabase;

    ArrayList<Spot> list;

    FirebaseUser user;

    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_spots);

        editSpotsBtn = findViewById(R.id.add_spot_btn);
        message = findViewById(R.id.message);
        mDatabase = DatabaseHelper.mDatabase();
        user = FirebaseAuth.getInstance().getCurrentUser();
        Request.onPressBack(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new RecycleViewAdapterSpots(this, list);
        recyclerView.setAdapter(adapter);

        //sett database changes listener
        //when changing recycler view will updates
        //if no data available, a message will be shown
        mDatabase.child("Organizations").child("spots").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                message.setVisibility(View.GONE);
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Spot spot = new Spot(dataSnapshot.getKey(), getEach(dataSnapshot,"name"), getEach(dataSnapshot,"charge"));
                    list.add(spot);
                }
                adapter.notifyDataSetChanged();

                if (!snapshot.exists()) {
                    message.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewSpotsActivity.this, ""+error, Toast.LENGTH_SHORT).show();
            }
        });


        //set on click listener for edit spots button
        editSpotsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewSpotsActivity.this, SpotsActivity.class);
                startActivity(intent);
            }
        });
    }

    //get each data from snapshot
    public String getEach (DataSnapshot snapshot, String name) {
        return snapshot.child(name).getValue().toString();
    }

}