package com.example.parkapp.recycler_view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.database.Request;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

//Adapting recycler view
//get data from main activity using a constructer
public class RecycleViewAdapterRequests extends RecyclerView.Adapter<ViewHolderRequests>{

    Context context;
    ArrayList<Request> list;

    Request request;

    AlertDialog.Builder builder;

    //constructor
    public RecycleViewAdapterRequests(Context context, ArrayList<Request> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolderRequests onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycler_view, parent, false);
        return new ViewHolderRequests(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRequests holder, int position) {
        builder = new AlertDialog.Builder(context);
        request = list.get(position);
        holder.requestId = request.getRequestId();
        holder.uId = request.getUid();
        DatabaseReference mDatabase = DatabaseHelper.mDatabase();

        if (request.getConfirmed()) {
            holder.directionsBtn.setVisibility(View.VISIBLE);
            holder.isConfirmedMsg.setVisibility(View.VISIBLE);

            holder.isAcceptedMsg.setVisibility(View.GONE);
            holder.confirmBtn.setVisibility(View.GONE);
        } else if (request.getAccepted()) {
            holder.isAcceptedMsg.setVisibility(View.VISIBLE);
            holder.confirmBtn.setVisibility(View.VISIBLE);
        } else {
            holder.isAcceptedMsg.setVisibility(View.GONE);
            holder.confirmBtn.setVisibility(View.GONE);
        }

        mDatabase.child("allSpots").child(request.getSpotId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.spotName.setText(snapshot.child("name").getValue().toString());
                    holder.charge.setText(Request.processCharge(snapshot.child("charge").getValue().toString()));
                    holder.latitude = snapshot.child("latitude").getValue().toString();
                    holder.longitude = snapshot.child("longitude").getValue().toString();
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
class ViewHolderRequests extends RecyclerView.ViewHolder{

    TextView spotName, charge, isAcceptedMsg, isConfirmedMsg;

    FirebaseUser user;

    Button deleteBtn, confirmBtn, directionsBtn;

    String requestId, uId, latitude, longitude;

    private RecycleViewAdapterRequests adapter;

    public ViewHolderRequests(@NonNull View itemView) {
        super(itemView);

        spotName = itemView.findViewById(R.id.spot_name);
        charge = itemView.findViewById(R.id.charge);
        isAcceptedMsg = itemView.findViewById(R.id.is_accepted);
        deleteBtn = itemView.findViewById(R.id.delete_btn);
        confirmBtn = itemView.findViewById(R.id.confirm_btn);
        directionsBtn = itemView.findViewById(R.id.directions_btn);
        isConfirmedMsg = itemView.findViewById(R.id.is_confirmed);


        //triggers when delete button clicked
        //after successful completion from database, item will remove from recycle view
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onDeleteBtnPress();
                    }
            }
        });

//      triggers when confirm button clicked
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    adapter.builder.setCancelable(true);
                    adapter.builder.setTitle("Confirm Parking Spot");
                    adapter.builder.setMessage("Are you sure? All other requests will be deleted");
                    adapter.builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onConfirmBtnPressed();
                                }
                            });
                    adapter.builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = adapter.builder.create();
                    dialog.show();
                }
            }
        });

//      triggers when get directions button clicked
        //user can get direction on google maps
        directionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=1"));
                intent.setPackage("com.google.android.apps.maps");

                if (intent.resolveActivity(adapter.context.getPackageManager()) != null) {
                    adapter.context.startActivity(intent);
                } else {
                    Toast.makeText(adapter.context, "Google Maps App not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





    //linking the adapter
    public ViewHolderRequests linkAdapter (RecycleViewAdapterRequests adapter) {
        this.adapter = adapter;
        return this;
    }

    //delete from firebase database
    public void onDeleteBtnPress () {
//        Log.i("DEL", ""+ adapter.list.get(getAdapterPosition()).getAccepted());
        if (adapter.list.get(getAdapterPosition()).getAccepted()) {
//            Log.i("DEL", "called");
            deleteBtnProcess(false, null, "accepted");
        }
        if (adapter.list.get(getAdapterPosition()).getAccepted() && adapter.list.get(getAdapterPosition()).getConfirmed() ) {
            deleteBtnProcess(true, "onPark", "confirmed");
        }
        if (!adapter.list.get(getAdapterPosition()).getAccepted() && !adapter.list.get(getAdapterPosition()).getConfirmed()){
//            Log.i("DEL", "not called");
            deleteBtnProcess(true, "spotRequests", "requests");
        }
    }

    public void deleteBtnProcess (boolean enabled, String locA, String locB) {
        DatabaseReference mDatabase = DatabaseHelper.mDatabase();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (enabled) {
            mDatabase.child("Organizations").child("data").child(uId).child("counts").child(locA).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    int value;
                    if (dataSnapshot.exists()) {
                        value = Integer.parseInt(dataSnapshot.getValue().toString()) - 1;
                    } else {
                        value = 0;
                    }

                    mDatabase.child("Organizations").child("data").child(uId).child("counts").child(locA).setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

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

        mDatabase.child("Owners").child(user.getUid()).child("requestedSpots").child("park").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mDatabase.child("Organizations").child("data").child(uId).child("park").child(locB).child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mDatabase.child("Owners").child(user.getUid()).child("userRequests").child("park").child(requestId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(adapter.context, "Request Removed", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemRemoved(getAdapterPosition());
                                adapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(adapter.context, "" + e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(adapter.context, "" + e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void onConfirmBtnPressed () {
        DatabaseReference mDatabase = DatabaseHelper.mDatabase();
        user = FirebaseAuth.getInstance().getCurrentUser();
        final boolean[] status = {false};
        for (int i = 0; adapter.list.size() > i; i++) {
            Request data = adapter.list.get(i);
            if (!Objects.equals(data.getRequestId(), requestId)) {

                mDatabase.child("Owners").child(user.getUid()).child("requestedSpots").child("park").child(data.getRequestId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mDatabase.child("Organizations").child("data").child(data.getUid()).child("counts").child("spotRequests").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                int value;
                                if (data.getAccepted()) {
                                    value = Integer.parseInt(dataSnapshot.getValue().toString());
                                } else {
                                    if (dataSnapshot.exists()) {
                                        value = Integer.parseInt(dataSnapshot.getValue().toString()) - 1;
                                    } else {
                                        value = 0;
                                    }
                                }

                                mDatabase.child("Organizations").child("data").child(data.getUid()).child("counts").child("spotRequests").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        if (data.getAccepted()) {
                                            mDatabase.child("Organizations").child("data").child(data.getUid()).child("park").child("accepted").child(data.getRequestId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    mDatabase.child("Owners").child(user.getUid()).child("userRequests").child("park").child(data.getRequestId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            status[0] = true;
                                                            adapter.notifyItemRemoved(getAdapterPosition());
                                                            adapter.notifyDataSetChanged();
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
                                            mDatabase.child("Organizations").child("data").child(data.getUid()).child("park").child("requests").child(data.getRequestId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    mDatabase.child("Owners").child(user.getUid()).child("userRequests").child("park").child(data.getRequestId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            status[0] = true;
                                                            adapter.notifyItemRemoved(getAdapterPosition());
                                                            adapter.notifyDataSetChanged();
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
                mDatabase.child("Organizations").child("data").child(data.getUid()).child("counts").child("onPark").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        int value;
                        if (dataSnapshot.exists()) {
                            value = Integer.parseInt(dataSnapshot.getValue().toString()) + 1;
                        } else {
                            value = 1;
                        }
                        mDatabase.child("Organizations").child("data").child(data.getUid()).child("counts").child("onPark").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                mDatabase.child("Organizations").child("data").child(data.getUid()).child("park").child("accepted").child(data.getRequestId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Request request = new Request(DatabaseHelper.userId(), data.getRequestId(), data.getSpotId(), true, true);
                                        mDatabase.child("Organizations").child("data").child(data.getUid()).child("park").child("confirmed").child(data.getRequestId()).setValue(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                mDatabase.child("Owners").child(user.getUid()).child("userRequests").child("park").child(data.getRequestId()).child("confirmed").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        status[0] = true;
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
        }

        Toast.makeText(adapter.context, "Spot confirmed", Toast.LENGTH_SHORT).show();
    }
}
