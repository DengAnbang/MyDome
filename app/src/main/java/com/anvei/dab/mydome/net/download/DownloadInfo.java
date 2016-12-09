package com.anvei.dab.mydome.net.download;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DAB on 2016/12/9 10:12.
 */

public class DownloadInfo {
    private int threadCount = 3;
    private String savePath;
    private String url;
    private long contentLength;
    private List<ThreadInfo> mThreadInfos;

    public String getUrl() {
        return url;
    }

    public String getSavePath() {
        return savePath;
    }

    public List<ThreadInfo> getThreadInfos() {
        return mThreadInfos;
    }

    public DownloadInfo(int threadCount, String savePath, String url, long contentLength) {
        this.threadCount = threadCount;
        this.savePath = savePath;
        this.url = url;
        this.contentLength = contentLength;
        mThreadInfos = new ArrayList<>();
        long length = contentLength / threadCount;
        for (int i = 0; i < threadCount; i++) {
            long start = length * i;
            long end = length * (i + 1) - 1;
            if (i == threadCount - 1) {
                end = contentLength-1;
            }
            mThreadInfos.add(new ThreadInfo(url,start, end, false));
        }
    }

    public class ThreadInfo {
        private long startLength;
        private long endLength;
        private boolean isFinish;
        private String url;
        private long bytesReaded;


        public ThreadInfo(String url, long startLength, long endLength, boolean isFinish) {
            this.url = url;
            this.startLength = startLength;
            this.endLength = endLength;
            this.isFinish = isFinish;
        }

        public long getBytesReaded() {
            return bytesReaded;
        }

        public void setBytesReaded(long bytesReaded) {
            this.bytesReaded = bytesReaded;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setFinish(boolean finish) {
            isFinish = finish;
        }

        public long getStartLength() {
            return startLength;
        }

        public long getEndLength() {
            return endLength;
        }

        public boolean isFinish() {
            return isFinish;
        }
    }


    public static class Builder {
        private int threadCount = 3;
        private String savePath;
        private String url;
        private long contentLength;

        public Builder setThreadCount(int threadCount) {
            if (threadCount < 1) {
                this.threadCount = 1;
            }
            if (threadCount > 10) {
                this.threadCount = 10;
            }
            this.threadCount = threadCount;
            return this;
        }

        public Builder setSavePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setContentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public DownloadInfo Build() {
            return new DownloadInfo(threadCount, savePath, url, contentLength);
        }
    }
}
