package com.example.shieldsecure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


import com.example.shieldsecure.Adapter.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "johny";
    public static final int STORAGE_PICTURE_REQUEST = 200;
    public static final int STORAGE_PERMISSION_SETTINGS_REQUEST = 300;

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initViewPager();
    }


    /**
     * A method to initialize the cool viewpager
     */
    private void initViewPager() {
        Log.d(TAG, "initViewPager: ");
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.bottom_menu_home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.bottom_menu_encrypt);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.bottom_menu_decrypt);
                        break;
                }
            }
        });
    }

    /**
     * A method to init the views
     */
    private void initViews() {
        viewPager = findViewById(R.id.main_LAY_pager);
        bottomNavigationView = findViewById(R.id.main_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_menu_home:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.bottom_menu_encrypt:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.bottom_menu_decrypt:
                        viewPager.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });
    }
}