package com.babis.bluetoothclient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationActivity extends AppCompatActivity {

    Url url = new Url();

    private EditText editTextUserName,editTextPassword;
    private Button btnRegister, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextUserName = findViewById(R.id.editTextUserName);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnCancel = findViewById(R.id.btnCancel);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editTextUserName.getText().toString().isEmpty() && !editTextPassword.getText().toString().isEmpty()){
                    String username = editTextUserName.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    RequestQueue requestQueue = Volley.newRequestQueue(RegistrationActivity.this);
                    JSONObject jsonObject = new JSONObject();

                    try {
                        jsonObject.put("username", username);
                        jsonObject.put("password", password);
                    }
                    catch (JSONException exception){
                        //Toast.makeText(getApplicationContext(),exception.getMessage(),Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(),"json error",Toast.LENGTH_LONG).show();
                    }

                    JsonObjectRequest objectRequest =new JsonObjectRequest(
                            Request.Method.POST,
                            url.getREGISTER_URL(),
                            jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    AlertDialog.Builder ad = new AlertDialog.Builder(RegistrationActivity.this);
                                        ad.
                                        setTitle("Registration successful").
                                        setMessage("Proceed to login. ").
                                        setCancelable(false).
                                        setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                                startActivity(intent);
                                                }
                                    })
                                     .show();

                                }
                                }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    try {
                                        //400: ("error": "Bad Request", Username already exists!)
                                        if (error.networkResponse.statusCode == 400){
                                            Toast.makeText(getApplicationContext(), " Username already exists!",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "Data have not been sent. \nUnknown error",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                            );
                            requestQueue.add(objectRequest);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Username or password can not be empty",Toast.LENGTH_LONG).show();
                    }
                }
            });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });

    }
}