package com.example.parkapp.recycler_view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkapp.R;
import com.example.parkapp.database.BookRequest;
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
public class RecycleViewAdapterBookRequests extends RecyclerView.Adapter<ViewHolderBookRequests>{

    Context context;
    ArrayList<BookRequest> list;

    String mode;

    int type;

    BookRequest request;

    //constructor
    public RecycleViewAdapterBookRequests(Context context, ArrayList<BookRequest> list, String mode, int type) {
        this.context = context;
        this.list = list;
        this.mode = mode;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolderBookRequests onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_requests, parent, false);
        return new ViewHolderBookRequests(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderBookRequests holder, int position) {
        request = list.get(position);
        holder.requestId = request.getRequestId();
        holder.uId = request.getUid();
        holder.spotId = request.getSpotId();
        holder.d = request.getDate();
        holder.fT = request.getFromTime();
        holder.tT = request.getToTime();
        holder.price = request.getPrice();
        DatabaseReference mDatabase = DatabaseHelper.mDatabase();

        if (mode.equals("owner")) {
            holder.confirmBtn.setVisibility(View.GONE);
            holder.declineBtn.setText("Remove");
            holder.pfp.setVisibility(View.GONE);
            holder.ownerName.setVisibility(View.GONE);
            holder.viewProfileLink.setVisibility(View.GONE);
            holder.profileText.setVisibility(View.GONE);
            holder.set1.setVisibility(View.GONE);
            holder.set2.setVisibility(View.GONE);
        }
        if (mode.equals("org") && type == 1) {
            holder.confirmBtn.setVisibility(View.GONE);
            holder.declineBtn.setText("Remove");
        }

        holder.date.setText(request.getDate());
        holder.toTime.setText(request.getToTime());
        holder.fromTime.setText(request.getFromTime());
        if (request.getFromTime().equals("Free")) {
            holder.displayPrice.setText("Free");
        } else {
            holder.displayPrice.setText("LKR " + request.getPrice());
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

        if (mode.equals("org")) {
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
        } else {
            mDatabase.child("userData").child(DatabaseHelper.userId()).addValueEventListener(new ValueEventListener() {
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



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}

//viewholder for all the item elements and functions
class ViewHolderBookRequests extends RecyclerView.ViewHolder{

    TextView spotName, charge, ownerName, vehicleType, vehicleModel, licensePlateNo, viewProfileLink, date, toTime, fromTime, profileText, displayPrice;

    LinearLayout set1, set2;

    ImageView pfp;

    Button declineBtn, confirmBtn;

    String requestId, uId, spotId, d, tT, fT, price;

    DatabaseReference mDatabase;


    private RecycleViewAdapterBookRequests adapter;

    public ViewHolderBookRequests(@NonNull View itemView) {
        super(itemView);

        spotName = itemView.findViewById(R.id.spot_name);
        charge = itemView.findViewById(R.id.charge);
        ownerName = itemView.findViewById(R.id.name_owner);
        vehicleType = itemView.findViewById(R.id.vehicle_type);
        vehicleModel = itemView.findViewById(R.id.vehicle_model);
        licensePlateNo = itemView.findViewById(R.id.license_plate_no);
        declineBtn = itemView.findViewById(R.id.decline_btn);
        confirmBtn = itemView.findViewById(R.id.confirm_btn);
        viewProfileLink = itemView.findViewById(R.id.view_profile);
        pfp = itemView.findViewById(R.id.pfp_owner);
        date = itemView.findViewById(R.id.date);
        toTime = itemView.findViewById(R.id.toTime);
        fromTime = itemView.findViewById(R.id.fromTime);
        profileText = itemView.findViewById(R.id.textView);
        displayPrice = itemView.findViewById(R.id.price);
        set1 = itemView.findViewById(R.id.set1);
        set2 = itemView.findViewById(R.id.set2);
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
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onConfirmBtnPress();
                }
            }
        });

    }





    //linking the adapter
    public ViewHolderBookRequests linkAdapter (RecycleViewAdapterBookRequests adapter) {
        this.adapter = adapter;
        return this;
    }

    public void onConfirmBtnPress () {
        mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child("bookRequests").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                int value;
                if (dataSnapshot.exists()) {
                    value = Integer.parseInt(dataSnapshot.getValue().toString()) - 1;
                } else {
                    value = 0;
                }
                mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child("bookRequests").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child("bookConfirmed").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                int value;
                                if (dataSnapshot.exists()) {
                                    value = Integer.parseInt(dataSnapshot.getValue().toString()) + 1;
                                } else {
                                    value = 1;
                                }
                                mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("counts").child("bookConfirmed").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        BookRequest requestForOrg = new BookRequest(uId, requestId, spotId, d, fT, tT, true, price);
                                        BookRequest requestForOwner = new BookRequest(DatabaseHelper.userId(), requestId, spotId, d, fT, tT, true, price);

                                        mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("book").child("confirmed").child(requestId).setValue(requestForOrg).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                mDatabase.child("Owners").child(uId).child("userRequests").child("book").child("requests").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        mDatabase.child("Owners").child(uId).child("userRequests").child("book").child("confirmed").child(requestId).setValue(requestForOwner).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("book").child("requests").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(adapter.context, "Request confirmed", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(adapter.context, "Request confirmed", Toast.LENGTH_SHORT).show();
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
            declineProcess("bookRequests", "requests", "Request declined");
        }
        if (adapter.type == 1) {
            declineProcess("bookConfirmed", "confirmed", "Booking removed");
        }
    }

    public void declineProcess (String locA, String locB, String toast) {
        if (adapter.mode.equals("org")) {
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
        } else {
            mDatabase.child("Organizations").child("data").child(uId).child("counts").child(locA).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    int value;
                    if (dataSnapshot.exists()) {
                        value = Integer.parseInt(dataSnapshot.getValue().toString()) - 1;
                    } else {
                        value = 0;
                    }

                    mDatabase.child("Organizations").child("data").child(uId).child("counts").child(locA).setValue(value).addOnFailureListener(new OnFailureListener() {
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

        if (adapter.mode.equals("org")) {
            mDatabase.child("Owners").child(uId).child("userRequests").child("book").child(locB).child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    mDatabase.child("Owners").child(uId).child("requestedSpots").child("book").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            mDatabase.child("Organizations").child("data").child(DatabaseHelper.userId()).child("book").child(locB).child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        } else {
            mDatabase.child("Organizations").child("data").child(uId).child("book").child(locB).child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    mDatabase.child("Owners").child(DatabaseHelper.userId()).child("requestedSpots").child("book").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            mDatabase.child("Owners").child(DatabaseHelper.userId()).child("userRequests").child("book").child(locB).child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
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
 }
