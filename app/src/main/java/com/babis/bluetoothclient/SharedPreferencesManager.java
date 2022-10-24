package com.babis.bluetoothclient;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String SHARED_PREF_NAME_USER = "userStorage";
    private static final String SHARED_PREF_NAME_TOKEN = "tokenStorage";
    private static final String SHARED_PREF_NAME_LOCATION = "locationStorage";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static  SharedPreferencesManager mInstance;
    private static Context ctx;

    private SharedPreferencesManager(Context context){
        ctx = context ;
    }

    public static synchronized SharedPreferencesManager getInstance(Context context){
        if (mInstance == null){
            mInstance = new SharedPreferencesManager(context);
        }
        return mInstance;
    }

    /**public void saveUser(User user){
        SharedPreferences sharedPreferencesUser = ctx.getSharedPreferences(SHARED_PREF_NAME_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorUser = sharedPreferencesUser.edit();
        editorUser.putString("username", user.getUsername());
        editorUser.apply();
    }**/

    //store the username in shared preferences
    public void saveUsername(String username){
        SharedPreferences sharedPreferencesUser = ctx.getSharedPreferences(SHARED_PREF_NAME_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorUser = sharedPreferencesUser.edit();
        editorUser.putString("username", username);
        editorUser.apply();
    }

    //return username
    public String getUsername(){
        SharedPreferences sharedPreferencesUser = ctx.getSharedPreferences(SHARED_PREF_NAME_USER, Context.MODE_PRIVATE);
        return sharedPreferencesUser.getString("username", null);
    }

    //this method will store the access_token and refresh_token in shared preferences
    public void saveTokens(Token token){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token.getAccess_token());
        editor.putString(KEY_REFRESH_TOKEN, token.getRefresh_token());
        editor.apply();
    }

    //this method will return the access_token and refresh_token
    public Token getTokens(){
        SharedPreferences sharedPreferences =ctx.getSharedPreferences(SHARED_PREF_NAME_TOKEN, Context.MODE_PRIVATE);
        return new Token(
                sharedPreferences.getString(KEY_ACCESS_TOKEN, null),
                sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        );
    }

    public void saveLocationData(double longitude, double latitude, String cityName, String countryName){
        SharedPreferences sharedPreferencesLocation = ctx.getSharedPreferences(SHARED_PREF_NAME_LOCATION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesLocation.edit();
        editor.putLong("longitude", Double.doubleToRawLongBits(longitude));
        editor.putLong("latitude", Double.doubleToRawLongBits(latitude));
        editor.putString("cityName", cityName);
        editor.putString("countryName", countryName);
        editor.apply();
    }

    public LocationData getLocationData(){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME_LOCATION, Context.MODE_PRIVATE);
        return new LocationData(
                Double.longBitsToDouble(sharedPreferences.getLong("longitude",0)),
                Double.longBitsToDouble(sharedPreferences.getLong("latitude",0)),
                sharedPreferences.getString("cityName", null),
                sharedPreferences.getString("countryName", null)
        );
    }

}
