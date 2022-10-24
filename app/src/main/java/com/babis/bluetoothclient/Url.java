package com.babis.bluetoothclient;

public class Url {

    private final String LOGIN_URL = "http://192.168.1.5:8080/login";
    private final String REGISTER_URL = "http://192.168.1.5:8080/api/user/save";
    private final String URL_VOLLEY_SEND_DATA = "http://192.168.1.5:8080/api/data/savedata";
    private final String REFRESH_TOKEN_REQUEST_URL = "http://192.168.1.5:8080/api/token/refresh";
    private final String getcountcities_Url = "http://192.168.1.5:8080/api/data/getcountcities";
    private final String GatheringInCountries_Url = "http://192.168.1.5:8080/api/data/gatheringInCountries";
    private final String GatheringByCountry_Url = "http://192.168.1.5:8080/api/data/gatheringInCitiesByCountry/";
    private final String GatheringByDay_Url = "http://192.168.1.5:8080/api/data/gatheringInCountriesByDay";
    private final String GatheringInCountryByDayFragment_Url =
            "http://192.168.1.5:8080/api/data/gatheringInCountryByDay/";
    private final String GatheringByCountryByDayFragment_Url =
            "http://192.168.1.5:8080/api/data/gatheringByCountryByDay/";
    private final String getLatLongByCountry_Url = "http://192.168.1.5:8080/api/data/latlongByCountry/";



    public Url() {
    }

    public String getLOGIN_URL() {
        return LOGIN_URL;
    }

    public String getREGISTER_URL() {
        return REGISTER_URL;
    }

    public String getURL_VOLLEY_SEND_DATA() {
        return URL_VOLLEY_SEND_DATA;
    }

    public String getREFRESH_TOKEN_REQUEST_URL() {
        return REFRESH_TOKEN_REQUEST_URL;
    }

    public String getGetcountcities_Url() {
        return getcountcities_Url;
    }

    public String getGatheringInCountries_Url() {
        return GatheringInCountries_Url;
    }

    public String getGatheringByCountry_Url() {
        return GatheringByCountry_Url;
    }

    public String getGatheringByDay_Url() {
        return GatheringByDay_Url;
    }

    public String getGatheringInCountryByDayFragment_Url() {
        return GatheringInCountryByDayFragment_Url;
    }

    public String getGatheringByCountryByDayFragment_Url() {
        return GatheringByCountryByDayFragment_Url;
    }

    public String getGetLatLongByCountry_Url() {
        return getLatLongByCountry_Url;
    }
}
