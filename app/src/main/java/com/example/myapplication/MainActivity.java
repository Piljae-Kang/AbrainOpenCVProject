package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    NameCardScan name_card;
    FileScan file_scan;
    Folders folders;
    Settings settings;
    mainFragment main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name_card = new NameCardScan();
        file_scan = new FileScan();
        folders = new Folders();
        settings = new Settings();
        main = new mainFragment();

        BottomNavigationView bottom_menu = findViewById(R.id.bottom_menu);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, main).commit();

        bottom_menu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.first_tab:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, name_card).commit();
                        return true;
                    case R.id.second_tab:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, file_scan).commit();
                        return true;
                    case R.id.third_tab:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, folders).commit();
                        return true;
                    case R.id.fourth_tab:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, settings).commit();
                        return true;
                }

                return false;
            }
        });  // 네비게이션 바 눌렀을 때 fragment 바꾸는 동작
    }
}