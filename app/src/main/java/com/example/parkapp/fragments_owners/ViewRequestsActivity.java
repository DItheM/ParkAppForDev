package com.example.parkapp.fragments_owners;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.recycler_view.RecycleViewAdapterParkRequests;
import com.example.parkapp.recycler_view.RecycleViewAdapterRequests;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewRequestsActivity extends AppCompatActivity {

    RecycleViewAdapterRequests adapter;

    RecyclerView recyclerView;

    DatabaseReference mDatabase;

    ArrayList<Request> list;

    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        message = findViewById(R.id.message);
        mDatabase = DatabaseHelper.mDatabase();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Request.onPressBack(this);

        list = new ArrayList<>();


        //sett database changes listener
        //when changing recycler view will updates
        //if no data available, a message will be shown
        if (DatabaseHelper.userId() != null) {
            mDatabase.child("Owners").child(DatabaseHelper.userId()).child("userRequests").child("park").addValueEventListener(new ValueEventListener() {
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
                    Toast.makeText(ViewRequestsActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "null user id", Toast.LENGTH_SHORT).show();
        }

        adapter = new RecycleViewAdapterRequests(this, list);
        recyclerView.setAdapter(adapter);
    }
}