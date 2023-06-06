package com.example.parkapp.fragments_organizations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.parkapp.AboutFragment;
import com.example.parkapp.R;
import com.example.parkapp.database.DatabaseHelper;
import com.example.parkapp.fragments_owners.DashboardOwnersFragment;
import com.example.parkapp.fragments_owners.HowToParkDemoFragment;
import com.example.parkapp.fragments_owners.MenuMainActivity;
import com.example.parkapp.fragments_owners.ProfileOwnersFragment;
import com.example.parkapp.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuMainOrganizationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    ProgressDialog progressDialog;

    DatabaseHelper databaseHelper;

    AlertDialog.Builder builder;

    FirebaseAuth mAuth;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_organization);

        Toolbar toolbar = findViewById(R.id.toolbar_organization);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        databaseHelper = new DatabaseHelper(this);
        builder = new AlertDialog.Builder(this);

        drawerLayout = findViewById(R.id.drawer_layout_organization);
        NavigationView navigationView = findViewById(R.id.nav_view_organization);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_organization, new DashboardOrganizationsFragment()).commit();
            navigationView.setCheckedItem(R.id.dashboard_organization);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dashboard_organization:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_organization, new DashboardOrganizationsFragment()).commit();
                break;

            case R.id.profile_organization:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_organization, new ProfileOrganizationsFragment()).commit();
                break;

            case R.id.about_organization:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_organization, new AboutFragment()).commit();
                break;

            case R.id.logout:
                logOut();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(false);
        }
    }

    public void logoutAction () {
        progressDialog.setMessage("Please wait while Logout...");
        progressDialog.setTitle("Logout");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isDeleted = databaseHelper.deleteDataOrg(user.getUid());
        if (isDeleted) {
            mAuth.signOut();
            progressDialog.dismiss();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MenuMainOrganizationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    public void logOut () {
        builder.setCancelable(true);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutAction();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
