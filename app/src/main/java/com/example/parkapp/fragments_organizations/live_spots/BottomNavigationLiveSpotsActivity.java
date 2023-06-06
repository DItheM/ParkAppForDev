package com.example.parkapp.fragments_organizations.live_spots;

import android.os.Bundle;

import com.example.parkapp.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.parkapp.database.Request;
import com.example.parkapp.databinding.ActivityBottomNavigationLiveSpotsBinding;

public class BottomNavigationLiveSpotsActivity extends AppCompatActivity {

    private ActivityBottomNavigationLiveSpotsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBottomNavigationLiveSpotsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Request.onPressBack(this);
        replaceFragment(new ParkRequestsFragment());

        binding.bottomNavView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.requests:
                    replaceFragment(new ParkRequestsFragment());
                    break;
                case R.id.accepted:
                    replaceFragment(new ParkAcceptedFragment());
                    break;
                case R.id.confirmed:
                    replaceFragment(new ParkConfirmedFragment());
                    break;
            }

            return true;
        });
    }

    private void replaceFragment (Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }

}