package com.babis.bluetoothclient;

public class LocationData {

    private double longitude, latitude;
    private String cityName, countryName;

    public LocationData(double longitude, double latitude, String cityName, String countryName) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.cityName = cityName;
        this.countryName = countryName;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }
}
