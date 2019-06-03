package com.dkarakaya.songify;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static TextView tvCurSongTitle;
    static TextView tvCurArtist;
    static LinearLayout lCurSongDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurSongTitle =findViewById(R.id.tvCurSongTitle);
        tvCurArtist =findViewById(R.id.tvCurSongArtist);
        lCurSongDetails=findViewById(R.id.curSongDetails);
        lCurSongDetails.setVisibility(View.GONE);


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LibraryFragment()).commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_lib:
                            selectedFragment = new LibraryFragment();
                            break;
                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    public static void setCurSongDetails(String curSongTitle, String curSongArtist){
        tvCurSongTitle.setText(curSongTitle);
        tvCurArtist.setText(curSongArtist);
        lCurSongDetails.setVisibility(View.VISIBLE);
    }
}
