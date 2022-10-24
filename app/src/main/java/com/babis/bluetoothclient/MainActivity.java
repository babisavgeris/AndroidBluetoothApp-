package com.babis.bluetoothclient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "MainActivity";

    private Context context;

    Geocoder geocoder;
    Location location;


    private static final String CHANNEL_ID = "Bluetooth Channel";
    private int id = 0;
    private boolean scheduled = false; //boolean to check if job Scheduler is called

    Url url = new Url();
    RequestQueue requestQueue;

    TextView textViewUsername, textViewNumOfDevices, textViewRisk, textViewCity, textViewCountry;
    RadioButton radioButtonOn, radioButtonOff;
    Button btnScan, btnSendData, btnGps, btnStats, scheduleJob, cancelSchedule;
    ImageView imageViewScan;

    ArrayList<String> stringArrayList = new ArrayList<String>(); //to store the number of devices bluetooth has found
    BluetoothAdapter myBluetoothAdapter;
    int status; // 0 = bluetooth is not discovering -> start discovery, 1 = bluetooth is discovering -> cancel discovery
    Intent bltEnablingIntent;
    int REQUEST_ENABLE_BT = 1;

    LocationManager locationManager;

    ProgressBar progressBarScan, progressBarGps;

    //data for spring boot
    private String username;
    private String cityName, countryName;
    private double longitude = 0.0 ;
    private double latitude = 0.0;
    private int numberOfDevices;

    ActivityResultLauncher<Intent> bluetoothEnablingLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //get username from shared preferences
        username = SharedPreferencesManager.getInstance(getApplicationContext()).getUsername();

        textViewUsername = findViewById(R.id.textViewUsername);
        textViewUsername.setText(username);

        textViewCountry = findViewById(R.id.textViewCountry);
        textViewCity = findViewById(R.id.textViewCity);
        textViewRisk = findViewById(R.id.textViewRisk);
        textViewNumOfDevices = findViewById(R.id.textViewNumOfDevices);
        radioButtonOn = findViewById(R.id.radioButtonOn);
        radioButtonOff = findViewById(R.id.radioButtonOff);
        btnScan = findViewById(R.id.btnScan);
        btnGps = findViewById(R.id.btnGps);
        btnSendData = findViewById(R.id.btnSendData);
        btnStats = findViewById(R.id.btnStats);
        scheduleJob = findViewById(R.id.scheduleJob);
        cancelSchedule = findViewById(R.id.cancelSchedule);
        imageViewScan = findViewById(R.id.imageViewScan);
        progressBarScan = findViewById(R.id.progressBarScan);
        progressBarScan.setVisibility(View.INVISIBLE);
        progressBarGps = findViewById(R.id.progressBarGps);
        progressBarGps.setVisibility(View.INVISIBLE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Quick permission check
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        bltEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();

        radioButtonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivityForResult is deprecated
                //if (!myBluetoothAdapter.isEnabled()) {
                //    startActivityForResult(bltEnablingIntent, REQUEST_ENABLE_BT);
                //} else {
                //    Toast.makeText(getApplicationContext(), "Bluetooth is already enabled.", Toast.LENGTH_LONG).show();
                //}

                bluetoothEnablingLauncher.launch(bltEnablingIntent);

            }
        });


        radioButtonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!myBluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(), "Bluetooth is already turned off", Toast.LENGTH_LONG);
                } else {
                    myBluetoothAdapter.disable();
                    Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == 0) {
                    if (radioButtonOff.isChecked()) {
                        Toast.makeText(getApplicationContext(), "Turn on bluetooth first", Toast.LENGTH_LONG).show();
                    } else {
                        //if (myBluetoothAdapter.isDiscovering()) {
                        //    myBluetoothAdapter.cancelDiscovery();
                        //}
                        stringArrayList.clear();
                        progressBarScan.setVisibility(View.VISIBLE);
                        myBluetoothAdapter.startDiscovery();
                        btnScan.setText("Cancel");
                        status = 1;
                    }
                } else {
                    progressBarScan.setVisibility(View.INVISIBLE);
                    myBluetoothAdapter.cancelDiscovery();
                    btnScan.setText("Scan for nearby devices");
                    status = 0;
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(myReceiver, intentFilter);


        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });

        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });

    }

    public void scheduleJob(View v) {

        scheduled = true;
        //check if api supports jobScheduler. must be greater than 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            ComponentName componentName = new ComponentName(this, JobService.class);
            JobInfo.Builder info = new JobInfo.Builder(123, componentName)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) //executed when there is wifi connection
                    .setPersisted(true)
                    .setPeriodic(15 * 60 * 1000);


            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info.build());
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Toast.makeText(getApplicationContext(), "Scan scheduled successfully", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Job scheduled successfully! ");
            } else {
                Log.d(TAG, "Job scheduling failed");
            }
        }
    }

    public void cancelSchedule(View v) {
        scheduled = false;
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Toast.makeText(getApplicationContext(), "Scan cancelled", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Job cancelled");

    }

    public void sendNotification(int numOfDevices) {

        createNotificationChannel();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Bluetooth scan")
                .setContentText("Nearby devices found: " + numOfDevices)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(id, notificationBuilder.build());

    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bluetooth Channel";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int d = device.getBluetoothClass().getDeviceClass();
                Log.d(TAG, "onReceive: " + device.getName() + " |  BluetoothClass " + d);

                if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART) {
                    stringArrayList.add(device.getName());
                }

            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(TAG, "Discovery finished ");

                progressBarScan.setVisibility(View.INVISIBLE);
                status = 0;
                btnScan.setText("Scan for nearby devices");
                try {
                    if (!stringArrayList.isEmpty()) {
                        numberOfDevices = stringArrayList.size();
                        textViewNumOfDevices.setText(String.valueOf(numberOfDevices));
                        Toast.makeText(getApplicationContext(), "Scan completed", Toast.LENGTH_LONG).show();

                        if (scheduled) {
                            sendNotification(numberOfDevices);
                            Log.d(TAG, "Location scheduler started ");
                            getLocationScheduler();

                        }
                        stringArrayList.clear();

                        if (numberOfDevices < 9) {
                            textViewRisk.setText("Low");
                            textViewRisk.setTextColor(Color.parseColor("green"));
                            imageViewScan.setBackgroundResource(R.drawable.greentick);
                        } else {
                            textViewRisk.setText("High");
                            textViewRisk.setTextColor(Color.parseColor("red"));
                            imageViewScan.setBackgroundResource(R.drawable.danger);
                        }
                    } else {
                        textViewNumOfDevices.setText("0");
                        textViewRisk.setText("Low");
                        textViewRisk.setTextColor(Color.parseColor("green"));
                        imageViewScan.setBackgroundResource(R.drawable.greentick);
                        Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_LONG).show();

                        //remove this when completed testing
                        if (scheduled) {
                            sendNotification(numberOfDevices);
                            Log.d(TAG, "Location scheduler started ");
                            getLocationScheduler();
                        }

                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        radioButtonOff.setChecked(true);
                        status = 0;
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        radioButtonOff.setChecked(true);
                        if (myBluetoothAdapter.isDiscovering()) {
                            myBluetoothAdapter.cancelDiscovery();
                        }
                        progressBarScan.setVisibility(View.INVISIBLE);
                        btnScan.setText("Scan for nearby devices");
                        status = 0;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        radioButtonOn.setChecked(true);
                        break;
                }
            }
        }
    };

    //deprecated!!!
    /**@Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_ENABLE_BT) {
    if (resultCode == RESULT_OK) {
    Toast.makeText(getApplicationContext(), "Bluetooth successfully enabled", Toast.LENGTH_LONG).show();
    } else if (resultCode == RESULT_CANCELED) {
    Toast.makeText(getApplicationContext(), "Bluetooth enabling is cancelled", Toast.LENGTH_LONG).show();
    radioButtonOff.setChecked(true);
    }
    }
    }**/

    public void sendData() {
        JSONObject jsonObjectData = new JSONObject();
        try {
            JSONObject objectUser = new JSONObject();
            objectUser.put("username", username);

            jsonObjectData.put("numOfPhones", numberOfDevices);
            jsonObjectData.put("longitude", longitude);
            jsonObjectData.put("latitude", latitude);
            jsonObjectData.put("city", cityName);
            jsonObjectData.put("country", countryName);
            jsonObjectData.put("user", objectUser);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url.getURL_VOLLEY_SEND_DATA(),
                jsonObjectData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Data sent successfully",
                                Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //403: access token is expired, request for new with refresh token method
                if (error.networkResponse.statusCode == 403) {
                    refreshToken();
                } else {
                    Toast.makeText(getApplicationContext(), "Data have not been sent. \nUnknown error",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Token token = SharedPreferencesManager.getInstance(getApplicationContext()).getTokens();
                String access_token = token.getAccess_token();
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + access_token);
                return headers;
            }
        };
        requestQueue.add(objectRequest);
    }

    //access token is expired, method to get new access token
    public void refreshToken() {
        JsonObjectRequest refreshTokenRequest = new JsonObjectRequest(
                Request.Method.GET,
                url.getREFRESH_TOKEN_REQUEST_URL(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Token token = new Token(
                                    response.getString("access_token"),
                                    response.getString("refresh_token")
                            );
                            SharedPreferencesManager.getInstance(getApplicationContext()).saveTokens(token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            sendData(); //make again the sendData request with the new access_token
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error: " + error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Token token = SharedPreferencesManager.getInstance(getApplicationContext()).getTokens();
                String refreshToken = token.getRefresh_token();
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + refreshToken);
                return headers;
            }
        };
        requestQueue.add(refreshTokenRequest);
    }

    public void getLocation(View view) {
        Boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGpsEnabled) {
            Toast.makeText(getApplicationContext(), "Please turn on GPS and try again!", Toast.LENGTH_LONG).show();
        } else {
            progressBarGps.setVisibility(View.VISIBLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    10,
                    (LocationListener) this);
        }

        Log.d(TAG, "getLocationScheduler: right before locationManager.getLastKnownLocation");
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null){
            progressBarGps.setVisibility(View.INVISIBLE);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d(TAG, "locationManager.removeUpdates ");
            locationManager.removeUpdates((LocationListener) this);

            try {
                geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
                List<Address> addresses = geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1);

                if (addresses.size() > 0){
                    cityName = addresses.get(0).getLocality();
                    countryName = addresses.get(0).getCountryName();
                    textViewCity.setText(addresses.get(0).getLocality());
                    textViewCountry.setText(countryName);
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    public void getLocationScheduler() {
        
            progressBarGps.setVisibility(View.VISIBLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

        Log.d(TAG, "getLocationScheduler: right before locationManager.requestLocationUpdates");

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    0,
                    (LocationListener) this);

        Log.d(TAG, "getLocationScheduler: right before locationManager.getLastKnownLocation");
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null){
                progressBarGps.setVisibility(View.INVISIBLE);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d(TAG, "locationManager.removeUpdates ");
                locationManager.removeUpdates((LocationListener) this);

                try {
                    geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1);

                    if (addresses.size() > 0){
                        cityName = addresses.get(0).getLocality();
                        countryName = addresses.get(0).getCountryName();
                        textViewCity.setText(addresses.get(0).getLocality());
                        textViewCountry.setText(countryName);
                    }
                    //locationManager.removeUpdates((LocationListener) this);


                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }

                Log.d(TAG, "getLocationScheduler: locationManager.removeUpdates from locationManager.getLastKnownLocation");
                sendData();
            }

        Log.d(TAG, "getLocationScheduler: right after locationManager.requestLocationUpdates");
        Log.d(TAG, "getLocationScheduler: heading towards onLocationChanged");

        //locationManager.removeUpdates((LocationListener) this);
        Log.d(TAG, "locationManager.removeUpdates ");

    }


    //check if bluetooth is supported on device and if it is enabled
    public void checkBluetoothState() {

        //check Bluetooth support on device
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            btnScan.setEnabled(false);
            radioButtonOn.setEnabled(false);
            radioButtonOff.setEnabled(false);
            scheduleJob.setEnabled(false); //this is the button with id = scheduleJob
            cancelSchedule.setEnabled(false); //this is the button with id = cancelSchedule
        } else {
            if (!myBluetoothAdapter.isEnabled()) {
                radioButtonOff.setChecked(true);
            } else {
                radioButtonOn.setChecked(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (!(myBluetoothAdapter == null)) {
            if (myBluetoothAdapter.isDiscovering()) {
                myBluetoothAdapter.cancelDiscovery();
            }
        }
        unregisterReceiver(myReceiver);

        super.onDestroy();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {


        Log.d(TAG, "onLocationChanged: entered onLocationChanged");
//        progressBarGps.setVisibility(View.INVISIBLE);
//        longitude = location.getLongitude();
//        latitude = location.getLatitude();
//
//        try {
//            geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
//            List<Address> addresses = geocoder.getFromLocation(
//            location.getLatitude(),
//            location.getLongitude(),
//            1);
//
//            if (addresses.size() > 0){
//            cityName = addresses.get(0).getLocality();
//            countryName = addresses.get(0).getCountryName();
//            textViewCity.setText(addresses.get(0).getLocality());
//            textViewCountry.setText(countryName);
//            }
//            locationManager.removeUpdates((LocationListener) this);
//                Log.d(TAG, "locationManager.removeUpdates ");
//
//            }
//            catch (Exception e){
//            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
//            }
//
//            Log.d(TAG, "onLocationChanged: right before if (scheduled)");
//
//            //job scheduler is active
//            if (scheduled){
//                 sendData();
//            }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

}