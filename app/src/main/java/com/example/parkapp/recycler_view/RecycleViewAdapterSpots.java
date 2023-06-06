package com.example.parkapp.recycler_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.database.Spot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

//Adapting recycler view
//get data from main activity using a constructer
public class RecycleViewAdapterSpots extends RecyclerView.Adapter<ViewHolderSpots>{

    Context context;
    ArrayList<Spot> list;

    //constructor
    public RecycleViewAdapterSpots(Context context, ArrayList<Spot> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolderSpots onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycler_view, parent, false);
        return new ViewHolderSpots(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderSpots holder, int position) {
        Spot spot = list.get(position);
        holder.spotName.setText(spot.getName());
        holder.charge.setText(Request.processCharge(spot.getCharge()));
        holder.spotId = spot.getSpotId();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

//viewholder for all the item elements and functions
class ViewHolderSpots extends RecyclerView.ViewHolder{

    TextView spotName, charge;

    DatabaseReference mDatabase;

    FirebaseUser user;

    String spotId;


    private RecycleViewAdapterSpots adapter;

    public ViewHolderSpots(@NonNull View itemView) {
        super(itemView);

        spotName = itemView.findViewById(R.id.spot_name);
        charge = itemView.findViewById(R.id.charge);

        //triggers when delete button clicked
        //after successful completion from database, item will remove from recycle view
        itemView.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        deleteDataFirebase(pos);
                    }
            }
        });
    }

    //linking the adapter
    public ViewHolderSpots linkAdapter (RecycleViewAdapterSpots adapter) {
        this.adapter = adapter;
        return this;
    }

    //delete from firebase database
    public void deleteDataFirebase (int pos) {
        mDatabase = DatabaseHelper.mDatabase();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.child("Organizations").child("spots").child(user.getUid()).child(spotId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mDatabase.child("allSpots").child(spotId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        adapter.notifyItemRemoved(getAdapterPosition());
                        adapter.notifyDataSetChanged();
                        Toast.makeText(adapter.context, "Spot Deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(adapter.context, ""+e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(adapter.context, ""+e, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
