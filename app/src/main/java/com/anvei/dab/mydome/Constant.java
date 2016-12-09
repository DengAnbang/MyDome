package com.anvei.dab.mydome;

import android.os.Environment;

import java.io.File;

/**
 * Created by DAB on 2016/12/7 09:23.
 */

public class Constant {
    public static final String baseUrl = "http://hengdawb-app.oss-cn-hangzhou.aliyuncs.com/";
    public static final String fileName = "app-debug.apk";
    public static final String fileStoreDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MAPP";
}
