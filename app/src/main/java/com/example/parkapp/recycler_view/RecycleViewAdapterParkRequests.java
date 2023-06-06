package com.example.parkapp.recycler_view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.example.parkapp.fragments_organizations.ViewVehicleOwnerProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//Adapting recycler view
//get data from main activity using a constructer
public class RecycleViewAdapterParkRequests extends RecyclerView.Adapter<ViewHolderParkRequests>{

    Context context;
    ArrayList<Request> list;

    int type;

    Request request;

    //constructor
    public RecycleViewAdapterParkRequests(Context context, ArrayList<Request> list, int type) {
        this.context = context;
        this.list = list;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolderParkRequests onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_park_requests_org, parent, false);
        return new ViewHolderParkRequests(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderParkRequests holder, int position) {
        request = list.get(position);
        holder.requestId = request.getRequestId();
        holder.uId = request.getUid();
        holder.spotId = request.getSpotId();
        DatabaseReference mDatabase = DatabaseHelper.mDatabase();

        if (type != 0) {
            holder.acceptBtn.setVisibility(View.GONE);
            holder.declineBtn.setText("Remove");
        }

        mDatabase.child("allSpots").child(request.getSpotId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.spotName.setText(snapshot.child("name").getValue().toString());
                    holder.charge.setText(Request.processCharge(snapshot.child("charge").getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, ""+error, Toast.LENGTH_SHORT).show();
            }
        });

        mDatabase.child("userData").child(request.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.ownerName.setText(snapshot.child("username").getValue().toString());
                    holder.vehicleType.setText(Request.getVehicleType(snapshot.child("vehicleType").getValue().toString()));
                    holder.licensePlateNo.setText(snapshot.child("licenseNo").getValue().toString());
                    holder.vehicleModel.setText(snapshot.child("model").getValue().toString());
                    Request.displayPFP(snapshot.child("pfpURL").getValue().toString(), holder.pfp, context);
                    holder.viewProfileLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), ViewVehicleOwnerProfileActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("name", snapshot.child("username").getValue().toString());
                            bundle.putString("phoneNo", snapshot.child("phoneNo").getValue().toString());
                            bundle.putString("licenseNo", snapshot.child("licenseNo").getValue().toString());
                            bundle.putString("type", Request.getVehicleType(snapshot.child("vehicleType").getValue().toString()));
                            bundle.putString("model", snapshot.child("model").getValue().toString());
                            bundle.putString("pfp", snapshot.child("pfpURL").getValue().toString());
                            intent.putExtras(bundle);
                            v.getContext().startActivity(intent);
                        }
                    });
                } else {
                    Toast.makeText(context, "Error getting user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, ""+error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

//viewholder for all the item elements and functions
class ViewHolderParkRequests extends RecyclerView.ViewHolder{

    TextView spotName, charge, ownerName, vehicleType, vehicleModel, licensePlateNo, viewProfileLink;

    ImageView pfp;

    Button declineBtn, acceptBtn;

    String requestId, uId, spotId;

    DatabaseReference mDatabase;


    private RecycleViewAdapterParkRequests adapter;

    public ViewHolderParkRequests(@NonNull View itemView) {
        super(itemView);

        spotName = itemView.findViewById(R.id.spot_name);
        charge = itemView.findViewById(R.id.charge);
        ownerName = itemView.findViewById(R.id.name_owner);
        vehicleType = itemView.findViewById(R.id.vehicle_type);
        vehicleModel = itemView.findViewById(R.id.vehicle_model);
        licensePlateNo = itemView.findViewById(R.id.license_plate_no);
        declineBtn = itemView.findViewById(R.id.decline_btn);
        acceptBtn = itemView.findViewById(R.id.confirm_btn);
        viewProfileLink = itemView.findViewById(R.id.view_profile);
        pfp = itemView.findViewById(R.id.pfp_owner);
        mDatabase = DatabaseHelper.mDatabase();

        //triggers when delete button clicked
        //after successful completion from database, item will remove from recycle view
        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onDeclineBtnPress();
                    }
            }
        });

//      triggers when confirm button clicked
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onAcceptBtnPress();
                }
            }
        });

    }





    //linking the adapter
    public ViewHolderParkRequests linkAdapter (RecycleViewAdapterParkRequests adapter) {
        this.adapter = adapter;
        return this;
    }

    public void onAcceptBtnPress () {
        mDatabase.child("Owners").child(uId).child("userRequests").child("park").child(requestId).child("accepted").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child("spotRequests").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        int value;
                        if (dataSnapshot.exists()) {
                            value = Integer.parseInt(dataSnapshot.getValue().toString()) - 1;
                        } else {
                            value = 0;
                        }

                        mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child("spotRequests").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Request request = new Request(uId, requestId, spotId, true, false);
                                mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("park").child("accepted").child(requestId).setValue(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("park").child("requests").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(adapter.context, "Request accepted", Toast.LENGTH_SHORT).show();
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(adapter.context, ""+e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDeclineBtnPress () {
        if (adapter.type == 0) {
            declineProcess(true, "spotRequests", "requests", "Request declined");
        }
        if (adapter.type == 1) {
            declineProcess(false, null, "accepted", "Request removed");
        }
        if (adapter.type == 2) {
            declineProcess(true, "onPark", "confirmed", "Request removed");
        }
    }

    public void declineProcess (boolean enabled, String locA, String locB, String toast) {
        if (enabled) {
            mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child(locA).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    int value;
                    if (dataSnapshot.exists()) {
                        value = Integer.parseInt(dataSnapshot.getValue().toString()) - 1;
                    } else {
                        value = 0;
                    }

                    mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child(locA).setValue(value).addOnFailureListener(new OnFailureListener() {
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

        mDatabase.child("Owners").child(uId).child("userRequests").child("park").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mDatabase.child("Owners").child(uId).child("requestedSpots").child("park").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("park").child(locB).child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(adapter.context, ""+toast, Toast.LENGTH_SHORT).show();
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(adapter.context, ""+e, Toast.LENGTH_SHORT).show();
            }
        });
    }
 }
