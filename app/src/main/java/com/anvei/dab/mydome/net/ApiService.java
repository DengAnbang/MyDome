package com.anvei.dab.mydome.net;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by DAB on 2016/12/7 09:10.
 *
 */

public interface ApiService {
    @Streaming
    @GET("{fileName}")
    Observable<ResponseBody> downloadFile(@Path("fileName") String fileName);

    @Streaming
    @GET("{fileName}")
    Call<ResponseBody> loadFile(@Path("fileName") String fileName);


    @Streaming
    @GET
    Observable<ResponseBody> download(@Header("RANGE") String start,@Url String url);

    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);
}
