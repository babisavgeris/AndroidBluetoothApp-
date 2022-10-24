package com.babis.bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText editTextUsername, editTextPassword;
    Button btnLogin, btnRegister;
    private final Url url = new Url();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RegistrationActivity.class);
                startActivity(intent);

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editTextUsername.getText().toString().isEmpty() && !editTextPassword.getText().toString().isEmpty()){
                    String username = editTextUsername.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            url.getLOGIN_URL(),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    try {
                                        JSONObject object = new JSONObject(response);

                                        Token token = new Token(
                                                object.getString("access_token"),
                                                object.getString("refresh_token")
                                        );
                                        SharedPreferencesManager.getInstance(getApplicationContext()).saveTokens(token);
                                        SharedPreferencesManager.getInstance(getApplicationContext()).saveUsername(username);
                                    }
                                    catch (JSONException exception) {
                                        exception.printStackTrace();
                                    }
                                    finally {
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //403: wrong username or password
                            if (error.networkResponse.statusCode == 403){
                                Toast.makeText(getApplicationContext(), "wrong username or password",
                                        Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Login failed. \n Unknown error..",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    ){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError{
                            Map<String, String> params = new HashMap<>();
                            params.put("username", username);
                            params.put("password", password);
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Username or password can not be empty",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}