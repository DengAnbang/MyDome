package com.anvei.dab.mydome.net;

import android.util.Log;

import com.anvei.dab.mydome.Constant;
import com.anvei.dab.mydome.net.download.DownloadInfo;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.anvei.dab.mydome.Constant.baseUrl;
import static com.anvei.dab.mydome.Constant.fileName;


/**
 * Created by DAB on 2016/12/7 09:18.
 */

public class NetClass {
    private static final String TAG = "NetClass";
    private static volatile NetClass instance;
    private final ApiService mApiService;
    private DownloadInfo mInfo;

    private NetClass() {
        mApiService = new Retrofit.Builder()
                .baseUrl(Constant.baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build()
                .create(ApiService.class);
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request())
                        .newBuilder()
                        .body(new DownloadResponseBody(chain.proceed(chain.request())))
                        .build())
                .build();

    }


    public static NetClass getInstance() {
        if (instance == null) {
            synchronized (NetClass.class) {
                if (instance == null) {
                    instance = new NetClass();
                }
            }
        }
        return instance;
    }

    public Disposable mDisposable;


    public void downloadFile(String url) {
        mInfo = getDownloadInfo();
        if (mInfo == null) {
            mApiService.download(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .flatMap(new Function<ResponseBody, ObservableSource<DownloadInfo.ThreadInfo>>() {
                        @Override
                        public ObservableSource<DownloadInfo.ThreadInfo> apply(ResponseBody responseBody) throws Exception {
                            List<DownloadInfo.ThreadInfo> threadInfos = getDownloadInfo(url, responseBody.contentLength()).getThreadInfos();
                            return Observable.fromIterable(threadInfos);
                        }
                    })
                    .flatMap(new Function<DownloadInfo.ThreadInfo, Observable<ResponseBody>>() {
                        @Override
                        public Observable<ResponseBody> apply(DownloadInfo.ThreadInfo threadInfo) throws Exception {
                            return getThreadDownload(threadInfo);
                        }
                    });




        }


    }

    private DownloadInfo getDownloadInfo() {

        return null;
    }

    private DownloadInfo getDownloadInfo(String url, long contentLength) {
        mInfo = new DownloadInfo.Builder()
                .setContentLength(contentLength)
                .setUrl(url)
                .setSavePath(Constant.fileStoreDir)
                .setThreadCount(5)
                .Build();
        return mInfo;
    }

    private void threadDownload(DownloadInfo info) {
        Observable<ResponseBody> empty = Observable.empty();
        for (int i = 0; i < info.getThreadInfos().size(); i++) {
            DownloadInfo.ThreadInfo threadInfo = info.getThreadInfos().get(i);
            if (!threadInfo.isFinish()) {
                empty.mergeWith(getThreadDownload(threadInfo));
            }
        }
        empty.subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(ResponseBody value) {
                Log.e(TAG, "onNext: ");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: ");
            }
        });
    }

    private Observable<ResponseBody> getThreadDownload(DownloadInfo.ThreadInfo threadInfo) {


        Log.e(TAG, "getThreadDownload: " + threadInfo.isFinish() + "***" + threadInfo.getStartLength() + "***" + threadInfo.getEndLength());
        Observable<ResponseBody> responseBodyObservable = getApiService(threadInfo).download("bytes=" + threadInfo.getStartLength() + "-" + threadInfo.getEndLength(), threadInfo.getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<ResponseBody, ResponseBody>() {
                    @Override
                    public ResponseBody apply(ResponseBody responseBody) throws Exception {
                        saveFile(threadInfo.getStartLength(), responseBody.byteStream()
                                , Constant.fileStoreDir, fileName);
                        return responseBody;
                    }
                });
        return responseBodyObservable;
    }

    private ApiService getApiService(DownloadInfo.ThreadInfo threadInfo) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .client(getOkHttpClient(threadInfo))
                .build()
                .create(ApiService.class);
    }

    private OkHttpClient getOkHttpClient(DownloadInfo.ThreadInfo threadInfo) {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return chain.proceed(chain.request()).newBuilder()
                                .body(new DownloadResponseBody(chain.proceed(chain.request()),threadInfo))
                                .build();
                    }
                }).build();
    }

    private void saveFile(long startIndex, InputStream inputStream, String fileStoreDir, String fileName) throws IOException {
        byte[] buf = new byte[2048];
        int len;
        try {
            File dir = new File(fileStoreDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(startIndex);
            while ((len = inputStream.read(buf)) != -1) {
                raf.write(buf, 0, len);
            }
//            fos.flush();
            raf.close();
            Log.e(TAG, "saveFile: 1239374*****" + file.length());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
