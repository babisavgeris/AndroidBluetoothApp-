package com.babis.bluetoothclient.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.github.mikephil.charting.charts.BarChart;
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

public class GatheringInCountryByDayFragment extends Fragment {

    private static final String FRAGMENT_NAME = GatheringInCountryByDayFragment.class.getSimpleName();

    View rootView;
    HorizontalBarChart chart;
    RequestQueue requestQueue;
    Url url = new Url();
    EditText editTextCountry;
    Button btnShowData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_gathering_in_country_by_day, container, false);
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

        btnShowData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDataFromServer();
            }
        });
    }

    private void loadDataFromServer() {

        String countryName = editTextCountry.getText().toString();
        String urlByCountry = url.getGatheringInCountryByDayFragment_Url()+countryName;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                urlByCountry,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("string", response);

                        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
                        ArrayList<String> labels  = new ArrayList<>();
                        labels.add("Monday");
                        labels.add("Tuesday");
                        labels.add("Wednesday");
                        labels.add("Thursday");
                        labels.add("Friday");
                        labels.add("Saturday");
                        labels.add("Sunday");

                        try {
                            JSONArray jsonArray = new JSONArray(response);


                            for (int i=0; i<jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String count = jsonObject.getString("count");
                                yVals1.add(new BarEntry(i,Float.parseFloat(count)));
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
                            set1 = new BarDataSet(yVals1, "Days");
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
        return GatheringInCountryByDayFragment.class.getSimpleName();
    }
}