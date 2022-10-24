package com.babis.bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.babis.bluetoothclient.fragment.GatheringByCountryByDayFragment;
import com.babis.bluetoothclient.fragment.GatheringByCountryFragment;
import com.babis.bluetoothclient.fragment.GatheringByDayFragment;
import com.babis.bluetoothclient.fragment.GatheringInCitiesFragment;
import com.babis.bluetoothclient.fragment.GatheringInCountriesFragment;
import com.babis.bluetoothclient.fragment.GatheringInCountryByDayFragment;

public class ChartActivity extends AppCompatActivity {

    Url url = new Url();
    RequestQueue requestQueue;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    TextView texView1;
    Button btnNext, btnPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        texView1 = findViewById(R.id.texView1);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

        requestQueue = Volley.newRequestQueue(ChartActivity.this);
        fragmentManager = getSupportFragmentManager();
        /**texView1.setText("Count: "+fragmentManager.getBackStackEntryCount());
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                texView1.setText("Count: "+fragmentManager.getBackStackEntryCount());
            }
        });**/

        btnPrevious.setVisibility(View.INVISIBLE);
        addSampleFragment();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFragment();
            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.popBackStack();
                if (fragmentManager.getBackStackEntryCount()==1){
                    btnPrevious.setVisibility(View.INVISIBLE);
                }
                if (btnNext.getVisibility() == View.INVISIBLE){
                    btnNext.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void addSampleFragment(){

        fragmentTransaction = fragmentManager.beginTransaction();
        GatheringInCountriesFragment fragment = new GatheringInCountriesFragment();
        fragmentTransaction.add(R.id.fragmentContainer, fragment);
        //fragmentTransaction.addToBackStack("Add " + fragment.toString());
        fragmentTransaction.commit();

    }

    private void addFragment(){
        Fragment fragment;
        /**switch (fragmentManager.getBackStackEntryCount()){
            case 0:
                fragment = new SampleFragment();
                break;
            case 1:
                fragment = new GatheringInCountriesFragment();
                btnPrevious.setVisibility(View.VISIBLE);
                break;
            case 2:
                fragment = new GatheringInCitiesFragment();
                break;
            default:
                fragment = new SampleFragment();
                break;
        }**/

        fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof GatheringInCountriesFragment){
            fragment = new GatheringInCitiesFragment();
            btnPrevious.setVisibility(View.VISIBLE);
        }else if (fragment instanceof GatheringInCitiesFragment){
            fragment = new GatheringByCountryFragment();

        }else if (fragment instanceof GatheringByCountryFragment){
            fragment = new GatheringByDayFragment();
        }else if (fragment instanceof GatheringByDayFragment){
            fragment = new GatheringInCountryByDayFragment();
            if (btnNext.getVisibility() == View.INVISIBLE){
                btnNext.setVisibility(View.VISIBLE);
            }
        }else if (fragment instanceof GatheringInCountryByDayFragment){
            fragment = new GatheringByCountryByDayFragment();
            btnNext.setVisibility(View.INVISIBLE);
        }
        else {
            fragment = new GatheringInCountriesFragment();
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer,fragment);
        fragmentTransaction.addToBackStack("Add " + fragment.toString());
        fragmentTransaction.commit();
    }

}