package com.example.parkapp.fragments_owners.view_books;

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
import com.example.parkapp.database.BookRequest;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.recycler_view.RecycleViewAdapterBookRequests;
import com.example.parkapp.recycler_view.RecycleViewAdapterParkRequests;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookRequestsFragment extends Fragment {

    String mode;

    RecycleViewAdapterBookRequests adapter;

    RecyclerView recyclerView;

    DatabaseReference mDatabase;

    ArrayList<BookRequest> list;

    TextView message;

    public BookRequestsFragment(String mode) {
        this.mode = mode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_requests, container, false);

        message = v.findViewById(R.id.message);
        mDatabase = DatabaseHelper.mDatabase();

        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();

        //sett database changes listener
        //when changing recycler view will updates
        //if no data available, a message will be shown
        if (DatabaseHelper.userId() != null) {
            if (mode.equals("owner")) {
                mDatabase.child("Owners").child(DatabaseHelper.userId()).child("userRequests").child("book").child("requests").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        message.setVisibility(View.GONE);
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            BookRequest request = dataSnapshot.getValue(BookRequest.class);
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
                mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("book").child("requests").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        message.setVisibility(View.GONE);
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            BookRequest request = dataSnapshot.getValue(BookRequest.class);
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
            }
        } else {
            Toast.makeText(getContext(), "null user id", Toast.LENGTH_SHORT).show();
        }

        adapter = new RecycleViewAdapterBookRequests(getContext(), list, mode, 0);
        recyclerView.setAdapter(adapter);

        return v;
    }
}