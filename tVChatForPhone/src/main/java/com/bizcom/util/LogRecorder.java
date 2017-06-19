package com.bizcom.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Reference to http://blog.csdn.net/way_ping_li/article/details/8487866
 * and improved some features...
 */
public class LogRecorder {

    private static final String TAG = "LogRecorder";

    public String mFileSuffix;
    public String mFolderPath;
    public int mFileSizeLimitation;

    private LogDumper mLogDumper = null;

    public static final int EVENT_RESTART_LOG = 1001;

    private RestartHandler mHandler;

    private static class RestartHandler extends Handler {
        final LogRecorder logRecorder;
        public RestartHandler(LogRecorder logRecorder) {
            this.logRecorder = logRecorder;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EVENT_RESTART_LOG) {
                logRecorder.stop();
                logRecorder.start();
            }
        }
    }

    public LogRecorder() {
        mHandler = new RestartHandler(this);
    }

    public void start() {
        // 清除缓存
        String cmd = "logcat -c";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(mFolderPath);
        File[] files = file.listFiles();
        if(files.length > 10){
            for (File temp : files) {
                temp.delete();
            }
        }

        String cmdStr = collectLogcatCommand();

        if (mLogDumper != null) {
            mLogDumper.stopDumping();
            mLogDumper = null;
        }

        mLogDumper = new LogDumper(mFolderPath, mFileSuffix, mFileSizeLimitation, cmdStr, mHandler);
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopDumping();
            mLogDumper = null;
        }
    }

    private String collectLogcatCommand() {
        return "logcat -b system -b main -b events -b radio -v time";
    }

    private class LogDumper extends Thread {
        final String logPath;
        final String logFileSuffix;
        final int logFileLimitation;
        final String logCmd;

        final RestartHandler restartHandler;

        private Process logcatProc;
        private BufferedReader mReader = null;
        private FileOutputStream out = null;

        private boolean mRunning = true;
        final private Object mRunningLock = new Object();

        private long currentFileSize;

        public LogDumper(String folderPath, String suffix,
                         int fileSizeLimitation, String command,
                         RestartHandler handler) {
            logPath = folderPath;
            logFileSuffix = suffix;
            logFileLimitation = fileSizeLimitation;
            logCmd = command;
            restartHandler = handler;

            String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
                    .format(new Date());
            String fileName = (TextUtils.isEmpty(logFileSuffix)) ? date : (logFileSuffix + "-"+ date);
            try {
                out = new FileOutputStream(new File(logPath, fileName + ".txt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void stopDumping() {
            synchronized (mRunningLock) {
                mRunning = false;
            }
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(logCmd);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && !line.isEmpty()) {
                        byte[] data = (line + "\n").getBytes();
                        out.write(data);
                        if (logFileLimitation != 0) {
	                        currentFileSize += data.length;
	                        if (currentFileSize > logFileLimitation * 1024) {
	                            restartHandler.sendEmptyMessage(EVENT_RESTART_LOG);
	                            break;
	                        }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }

    public static class Builder {

        /**
         * context object
         */
        private Context mContext;

        /**
         * the whole folder path that we save log files to,
         * this setting`s priority is bigger than folder name.
         */
        private String mLogFolderPath;

        /**
         * the log file suffix,
         * if this is sot, it will be appended to log file name automatically
         */
        private String mLogFileNameSuffix = "";

        /**
         * single log file size limitation,
         * in k-bytes, ex. set to 16, is 16KB limitation.
         */
        private int mLogFileSizeLimitation = 0;

        /**
         * log output format, don`t support config yet, use $time format as default.
         * <p/>
         * Log messages contain a number of metadata fields, in addition to the tag and priority.
         * You can modify the output format for messages so that they display a specific metadata
         * field. To do so, you use the -v option and specify one of the supported output formats
         * listed below.
         * <p/>
         * brief      — Display priority/tag and PID of the process issuing the message.
         * process    — Display PID only.
         * tag        — Display the priority/tag only.
         * thread     - Display the priority, tag, and the PID(process ID) and TID(thread ID)
         * of the thread issuing the message.
         * raw        — Display the raw log message, with no other metadata fields.
         * time       — Display the date, invocation time, priority/tag, and PID of
         * the process issuing the message.
         * threadtime — Display the date, invocation time, priority, tag, and the PID(process ID)
         * and TID(thread ID) of the thread issuing the message.
         * long       — Display all metadata fields and separate messages with blank lines.
         */
        private int mLogOutFormat;

        /**
         * set log out folder path
         *
         * @param logFolderPath out folder absolute path
         * @return the same Builder
         */
        public Builder setLogFolderPath(String logFolderPath) {
            this.mLogFolderPath = logFolderPath;
            return this;
        }

        /**
         * set log file name suffix
         *
         * @param logFileNameSuffix auto appened suffix
         * @return the same Builder
         */
        public Builder setLogFileNameSuffix(String logFileNameSuffix) {
            this.mLogFileNameSuffix = logFileNameSuffix;
            return this;
        }

        /**
         * set the file size limitation
         *
         * @param fileSizeLimitation file size limitation in KB
         * @return the same Builder
         */
        public Builder setLogFileSizeLimitation(int fileSizeLimitation) {
            this.mLogFileSizeLimitation = fileSizeLimitation;
            return this;
        }

        /**
         * sets log out format, -v parameter
         *
         * @param logOutFormat out format, like -v time
         * @return the same Builder
         */
        public Builder setLogOutFormat(int logOutFormat) {
            this.mLogOutFormat = mLogOutFormat;
            return this;
        }

        public Builder(Context context) {
            mContext = context;
        }

        /**
         * Combine all of the options that have been set and return
         * a new {@link LogRecorder} object.
         */
        public LogRecorder build() {
            LogRecorder logRecorder = new LogRecorder();
            logRecorder.mFolderPath = mLogFolderPath;
            logRecorder.mFileSuffix = mLogFileNameSuffix;
            logRecorder.mFileSizeLimitation = mLogFileSizeLimitation;
            return logRecorder;
        }
    }

}