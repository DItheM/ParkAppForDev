package com.example.parkapp.fragments_owners.view_books;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.parkapp.R;
import com.example.parkapp.database.Request;
import com.example.parkapp.databinding.ActivityBottomNavigationLiveSpotsBinding;
import com.example.parkapp.databinding.ActivityBottomNavigationViewBooksBinding;
import com.example.parkapp.fragments_organizations.live_spots.ParkAcceptedFragment;
import com.example.parkapp.fragments_organizations.live_spots.ParkConfirmedFragment;
import com.example.parkapp.fragments_organizations.live_spots.ParkRequestsFragment;

public class BottomNavigationViewBooksActivity extends AppCompatActivity {

    private ActivityBottomNavigationViewBooksBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBottomNavigationViewBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        String mode = bundle.getString("mode");
        String upcoming = bundle.getString("upcoming");

        Request.onPressBack(this);

        if (upcoming != null) {
            replaceFragment(new BookConfirmedFragment(mode));
            binding.bottomNavView.setSelectedItemId(R.id.confirmed);
        } else {
            replaceFragment(new BookRequestsFragment(mode));
            binding.bottomNavView.setSelectedItemId(R.id.requests);
        }




        binding.bottomNavView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.requests:
                    replaceFragment(new BookRequestsFragment(mode));
                    break;
                case R.id.confirmed:
                    replaceFragment(new BookConfirmedFragment(mode));
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