package com.kunalbadole.covid_tracker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {

    //base url of the site
    static String BASE_URL = "https://corona.lmao.ninja/v2/";

    //here we are using the retrofit to fetch the data from the API

    @GET("countries")
    Call<List<ModelClass>> getcountrydata();

}
