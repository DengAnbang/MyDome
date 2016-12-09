package com.anvei.dab.mydome.net.download;

/**
 * Created by DAB on 2016/12/9 15:38.
 */

public interface IDownloadCallback {

    default void onProgressChange(long progress, long total){

    }

    default void onPauseDownload(long haveDownloaded) {

    }
}
