package com.babis.bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.babis.bluetoothclient.fragment.MapsFragment;

public class MapActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fragmentManager = getSupportFragmentManager();

        addMapsFragment();
    }

    public void addMapsFragment(){
        fragmentTransaction = fragmentManager.beginTransaction();
        MapsFragment mapsFragment = new MapsFragment();
        fragmentTransaction.add(R.id.fragmentContainer, mapsFragment);
        fragmentTransaction.commit();
    }

}