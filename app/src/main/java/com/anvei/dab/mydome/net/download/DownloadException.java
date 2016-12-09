package com.anvei.dab.mydome.net.download;

/**
 * Created by DAB on 2016/12/9 14:50.
 */

public class DownloadException extends Exception{
    private static final int error_Code_stop_Download = 0;
    private int errorCode;
    public DownloadException(int errorCode,String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
