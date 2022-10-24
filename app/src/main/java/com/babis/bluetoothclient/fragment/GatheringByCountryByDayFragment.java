package com.babis.bluetoothclient.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GatheringByCountryByDayFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String FRAGMENT_NAME = GatheringByCountryByDayFragment.class.getSimpleName();

    View rootView;
    HorizontalBarChart chart;
    RequestQueue requestQueue;
    Url url = new Url();
    EditText editTextCountry;
    Button btnShowData;

    private int day = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_gathering_by_country_by_day, container, false);
        initUI();
        return rootView;
    }

    private void initUI() {
        chart = (HorizontalBarChart) rootView.findViewById(R.id.chart);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setNoDataText("Insert Country");
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        editTextCountry = rootView.findViewById(R.id.editTextCountry);
        btnShowData = rootView.findViewById(R.id.btnShowData);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.days_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        btnShowData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**if (day == 0 || editTextCountry.getText().equals("")){
                    Toast.makeText(getActivity().getApplicationContext(),"Select both day and country first!",Toast.LENGTH_LONG).show();
                }else {
                    loadDataFromServer();
                }**/
                loadDataFromServer();
            }
        });
    }

    private void loadDataFromServer() {
        String countryName = editTextCountry.getText().toString();
        String url1 = url.getGatheringByCountryByDayFragment_Url()+countryName+"/"+day;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url1,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("string", response);

                        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
                        ArrayList<String> labels  = new ArrayList<String>();

                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            int j=0;
                            for (int i=(jsonArray.length())-1; i>=0; i--){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String city = jsonObject.getString("city").trim();
                                String count = jsonObject.getString("count").trim();

                                yVals1.add(new BarEntry(j++,Float.parseFloat(count)));
                                labels .add(city);
                            }

                            XAxis xl = chart.getXAxis();
                            xl.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xl.setDrawAxisLine(true);
                            xl.setDrawGridLines(false);
                            xl.setValueFormatter(new IndexAxisValueFormatter(labels));
                            xl.setLabelCount(labels.size());
                            xl.setGranularity(1);

                            YAxis yl = chart.getAxisLeft();
                            yl.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
                            yl.setDrawGridLines(false);
                            yl.setEnabled(false);
                            yl.setAxisMinimum(0f);

                            YAxis yr = chart.getAxisRight();
                            yr.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
                            yr.setDrawGridLines(false);
                            yr.setAxisMinimum(0f);

                            BarDataSet set1;
                            set1 = new BarDataSet(yVals1, "Cities");
                            set1.setColors(ColorTemplate.COLORFUL_COLORS);
                            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                            dataSets.add(set1);
                            BarData data = new BarData(dataSets);
                            data.setValueTextSize(10f);
                            data.setBarWidth(.9f);
                            chart.setData(data);
                            chart.setTouchEnabled(false);
                            chart.getDescription().setText("Gathering in " + countryName);

                            chart.invalidate();

                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }


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
        requestQueue.add(stringRequest);

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
                            loadDataFromServer();
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

    @NonNull
    @Override
    public String toString() {
        return GatheringByCountryByDayFragment.class.getSimpleName();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        /**
         position(pos):  0 = select day
                    1 = monday
                    2 = tuesday
                    3 = wednesday
                        .
                        .
                    7 = sunday
         **/

        //int position = (int) parent.getItemAtPosition(pos);
        switch (pos){
            case 0:
                if (!(day==0)){
                    day = 0;
                }
                break;
            case 1:
                day = 1;
                break;
            case 2:
                day = 2;
                break;
            case 3:
                day = 3;
                break;
            case 4:
                day = 4;
                break;
            case 5:
                day = 5;
                break;
            case 6:
                day = 6;
                break;
            case 7:
                day = 7;
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Another interface callback
    }
}