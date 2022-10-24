package com.babis.bluetoothclient.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.babis.bluetoothclient.R;
import com.babis.bluetoothclient.SharedPreferencesManager;
import com.babis.bluetoothclient.Token;
import com.babis.bluetoothclient.Url;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment {

    private static final String FRAGMENT_NAME = MapsFragment.class.getSimpleName();

    private String countryName;
    public MapsFragment(String countryName){
        this.countryName = countryName;
    }

    public MapsFragment() {
    }

    View rootView;
    RequestQueue requestQueue;
    Url url = new Url();
    ArrayList<Double> latLong = new ArrayList<>();
    double latitude, longitude;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //LatLng sydney = new LatLng(-34, 151);
            //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            /**
             @Overridepublic void onMapReady(GoogleMap map)
             {    map.addMarker(	new MarkerOptions().position(new
             LatLng(parsedLat,parsedLong).title("Hello world"));
             }
             **/

            requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

            //String countryName = "Greece";
            String urlLatLongByCountry = url.getGetLatLongByCountry_Url()+countryName;
            StringRequest request = new StringRequest(
                    Request.Method.GET,
                    urlLatLongByCountry,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            latitude = 0.0;
                            longitude = 0.0;

                            try {

                                JSONArray jsonArray = new JSONArray(response);
                                for (int i=0; i<jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    latitude = jsonObject.getDouble("latitude");
                                    longitude = jsonObject.getDouble("longitude");
                                    googleMap.addMarker(new MarkerOptions().position(new
                                            LatLng(latitude, longitude)));
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            LatLng zoom = new LatLng(latitude, longitude);
                            googleMap.addMarker(new MarkerOptions().position(zoom).title("Marker in " + countryName));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(zoom));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //403: access token is expired, request for new with refresh token method
                    if (error.networkResponse.statusCode == 403){
                        //String loadDataFromServer = "loadDataFromServer";
                        refreshToken();
                    }
                    else {
                        Toast.makeText(getActivity().getApplicationContext(), error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Token token = SharedPreferencesManager.getInstance(getActivity().getApplicationContext()).getTokens();
                    String access_token = token.getAccess_token();
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + access_token);
                    return headers;
                }
            };
            requestQueue.add(request);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        return rootView;
    }


    private void refreshToken() {
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
                            SharedPreferencesManager.getInstance(getActivity().getApplicationContext()).saveTokens(token);

                        }
                        catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        }finally {
                            
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getActivity().getApplicationContext(),
                        "Error: " + error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        ){
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Token token = SharedPreferencesManager.getInstance(getActivity().getApplicationContext()).getTokens();
                String refreshToken = token.getRefresh_token();
                Map<String,String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + refreshToken);
                return headers;
            }
        };
        requestQueue.add(refreshTokenRequest);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return MapsFragment.class.getSimpleName();
    }
}