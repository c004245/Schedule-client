package com.example.hyunwook.schedulermacbooktroops.login;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 18-09-27 Login SignUp
 */
public class RequestRegister {

    private static RequestRegister ourInstance = new RequestRegister();
    public static RequestRegister getInstance() {
        return ourInstance;
    }

    private RequestRegister() {

    }
    /**
     * name, emaill, password, password_confirmation, phone, birth
     */
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://schedule.mactroops.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    RetrofitRegister service = retrofit.create(RetrofitRegister.class);

    public RetrofitRegister getService() {
        return service;
    }
}