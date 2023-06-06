package com.example.parkapp.fragments_owners;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.parkapp.R;
import com.example.parkapp.fragments_owners.view_books.BottomNavigationViewBooksActivity;
import com.example.parkapp.ui.login.LoginActivity;

import java.util.Objects;

public class DashboardOwnersFragment extends Fragment implements SensorEventListener {

    LinearLayout searchForParkBtn, humiditySection, bookForParkBtn;

    Button upcomingBookingsButton;

    TextView humidity;

    private SensorManager sensorManager;
    private Sensor sensorDetails;
    private boolean isSensorAvailable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dashboard_owners, container, false);

        //getting access for layout elements
        searchForParkBtn = v.findViewById(R.id.view3);
        humidity = v.findViewById(R.id.textView12);
        humiditySection = v.findViewById(R.id.humidity);
        bookForParkBtn = v.findViewById(R.id.view4);
        upcomingBookingsButton = v.findViewById(R.id.upcoming_bookings_button);

        //set onclicklistener for searchFor Park Button
        searchForParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchForParkActivity.class);
                startActivity(intent);
            }
        });

        //set onclicklistener for bookFor Park Button
        bookForParkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookForParkActivity.class);
                startActivity(intent);
            }
        });

        //set onclicklistener for upcoming bookings Button
        upcomingBookingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BottomNavigationViewBooksActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("mode", "owner");
                bundle.putString("upcoming", "true");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //humidity sensor functions
        sensorManager = (SensorManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            sensorDetails = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            isSensorAvailable = true;
            humiditySection.setVisibility(View.VISIBLE);
        } else {
            isSensorAvailable = false;
            humiditySection.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        humidity.setText(String.valueOf(Math.round(event.values[0])));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSensorAvailable) {
            sensorManager.registerListener(this, sensorDetails, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isSensorAvailable) {
            sensorManager.unregisterListener((this));
        }
    }
}