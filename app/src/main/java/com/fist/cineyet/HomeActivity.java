package com.fist.cineyet;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        BottomNavigationView botNav=findViewById(R.id.bottom_nav);
        botNav.setOnNavigationItemSelectedListener(myListener);

        Intent intent=getIntent();
        if(intent.hasExtra("name")){
            Fragment profFrag=new profile_page();
            Bundle mybund=new Bundle();
            mybund.putString("updatedName",intent.getStringExtra("name"));
            mybund.putString("updatedUsername",intent.getStringExtra("username"));
            mybund.putString("updatedInterests",intent.getStringExtra("interests"));
            mybund.putString("updatedEmail",intent.getStringExtra("email"));
            mybund.putString("isPersonalProfile","PERSONAL");
            profFrag.setArguments(mybund);
            botNav.setSelectedItemId(R.id.nav_profile);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,profFrag).commit();

        }
        else
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener myListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFrag=null;
            switch(menuItem.getItemId()){
                case R.id.nav_home:
                    selectedFrag=new HomeFragment();
                    break;
                case R.id.nav_hot:
                    selectedFrag=new HotFragment();
                    break;
                case R.id.nav_profile:
                    Bundle myBund=new Bundle();
                    myBund.putString("isPersonalProfile","PERSONAL");
                    selectedFrag=new profile_page();
                    selectedFrag.setArguments(myBund);

                    break;

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFrag).commit();

            return true;

        }
    };

    /* Ensures user does not return to log in screen after logging in the first time and being redirected to the home page*/
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}


