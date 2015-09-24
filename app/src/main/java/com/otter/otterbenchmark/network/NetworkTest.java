package com.otter.otterbenchmark.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.otter.otterbenchmark.R;
import com.otter.otterbenchmark.Util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Analyze network bandwidth.
 *
 * Read file from network and record spent time. Afterwards, calculate the speed.
 */
public class NetworkTest extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = NetworkTest.class.getSimpleName();

    private static final String[] URL_LIST = {
            "http://download.thinkbroadband.com/5MB.zip",
            "http://download.thinkbroadband.com/50MB.zip",
    };

    private static final long UPDATE_FREQUENCY = 1000;

    private MainHandler mMainHandler = new MainHandler(this);
    private static final int MSG_TEST_BEGIN = 0;
    private static final int MSG_TEST_FINISH = 1;
    private static final int MSG_TEST_FAIL = 2;
    private static final int MSG_TEST_CANCEL = 3;
    private static final int MSG_CONNECT_SUCCESS = 4;
    private static final int MSG_CONNECT_FAIL = 5;
    private static final int MSG_DOWNLOADING = 6;
    private static final int MSG_PERIOD_UPDATE = 7;

    private Spinner url_spinner;
    private TextView connection_time;
    private TextView file_size;
    private TextView elapsed_time;
    private TextView download_speed;
    private TextView status;
    private Button test_start;
    private Button test_cancel;

    private long mConnectionTime = -1;
    private long mFileSize = -1;
    private long mElapsedTime = -1;
    private long mDownloadSpeed = -1;

    private boolean mRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_network_test);
        findViews();
        setSpinner();
        setListener();

        url_spinner.setSelection(1);
    }

    private void findViews() {
        url_spinner = (Spinner) findViewById(R.id.url_spinner);
        connection_time = (TextView) findViewById(R.id.connection_time);
        file_size = (TextView) findViewById(R.id.file_size);
        elapsed_time = (TextView) findViewById(R.id.elapsed_time);
        download_speed = (TextView) findViewById(R.id.download_speed);
        status = (TextView) findViewById(R.id.status);
        test_start = (Button) findViewById(R.id.test_start);
        test_cancel = (Button) findViewById(R.id.test_cancel);
    }

    private void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, URL_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        url_spinner.setAdapter(adapter);
    }

    private void setListener() {
        test_start.setOnClickListener(this);
        test_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_start:
                mRunning = true;
                new Thread(mTestTask).start();
                break;
            case R.id.test_cancel:
                mRunning = false;
                break;
            default:
                break;
        }
    }

    private Runnable mTestTask = new Runnable() {

        @Override
        public void run() {
            sendMainHandlerMsg(MSG_TEST_BEGIN);

            InputStream inputStream = null;

            try {
                // Connecting
                long start = System.currentTimeMillis();
                URL url = new URL(url_spinner.getSelectedItem().toString());
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setUseCaches(false);
                urlConn.addRequestProperty("Cache-Control", "no-cache");
                urlConn.connect();
                mConnectionTime = System.currentTimeMillis() - start;

                // Get file size
                mFileSize = (long) urlConn.getContentLength();

                // Check connection status
                if (mFileSize < 0 || urlConn.getResponseCode() == 404) {
                    Log.w(TAG, "HTTP connect failed");
                    sendMainHandlerMsg(MSG_CONNECT_FAIL);
                    return;
                } else {
                    sendMainHandlerMsg(MSG_CONNECT_SUCCESS);
                }

                // Downloading
                sendMainHandlerMsg(MSG_DOWNLOADING);
                inputStream = urlConn.getInputStream();
                byte[] buffer = new byte[1024 * 8];
                start = System.currentTimeMillis();
                int byteCount;
                int periodByteCount = 0;
                long periodStart = start;
                long periodElapsed;

                while ((byteCount = inputStream.read(buffer)) != -1) {
                    if (!mRunning) {
                        break;
                    }

                    // Calculate the download speed in each UPDATE_FREQUENCY.
                    periodByteCount += byteCount;
                    periodElapsed = System.currentTimeMillis() - periodStart;
                    if (periodElapsed > UPDATE_FREQUENCY) {
                        mElapsedTime = System.currentTimeMillis() - start;
                        mDownloadSpeed = periodByteCount * 1000L / periodElapsed;
                        sendMainHandlerMsg(MSG_PERIOD_UPDATE);

                        // Reset period parameters
                        periodStart = System.currentTimeMillis();
                        periodByteCount = 0;
                    }
                }

                if (mRunning) {
                    // Download finish
                    mElapsedTime = System.currentTimeMillis() - start;
                    mDownloadSpeed = mFileSize * 1000L / mElapsedTime;
                    sendMainHandlerMsg(MSG_TEST_FINISH);
                } else {
                    // Download cancel
                    sendMainHandlerMsg(MSG_TEST_CANCEL);
                }
            } catch (IOException e) {
                Log.w(TAG, "Http connect failed", e);
                sendMainHandlerMsg(MSG_TEST_FAIL);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TEST_BEGIN:
                status.setText("Test beginning...");
                test_start.setEnabled(false);

                // Reset information
                connection_time.setText("");
                file_size.setText("");
                elapsed_time.setText("");
                download_speed.setText("");
                break;
            case MSG_TEST_FINISH:
                status.setText("Test finished.");
                test_start.setEnabled(true);
                elapsed_time.setText(Util.convertMillisecondToTime(mElapsedTime));
                download_speed.setText(Util.getSizeString(mDownloadSpeed) + "/s");
                break;
            case MSG_TEST_FAIL:
                status.setText("Test failed.");
                test_start.setEnabled(true);
                break;
            case MSG_TEST_CANCEL:
                status.setText("Test cancel.");
                test_start.setEnabled(true);
                break;
            case MSG_CONNECT_SUCCESS:
                status.setText("Connect successfully.");
                connection_time.setText(Util.convertMillisecondToTime(mConnectionTime));
                file_size.setText(Util.getSizeString(mFileSize));
                break;
            case MSG_CONNECT_FAIL:
                status.setText("Connect failed.");
                test_start.setEnabled(true);
                break;
            case MSG_DOWNLOADING:
                status.setText("Downloading...");
                break;
            case MSG_PERIOD_UPDATE:
                elapsed_time.setText(Util.convertMillisecondToTime(mElapsedTime));
                download_speed.setText(Util.getSizeString(mDownloadSpeed) + "/s");
            default:
                break;
        }
    }

    private void sendMainHandlerMsg(int msgType) {
        if (mMainHandler != null) {
            Message msg = mMainHandler.obtainMessage();
            msg.what = msgType;
            mMainHandler.sendMessage(msg);
        }
    }

    private static class MainHandler extends Handler {
        private final WeakReference<NetworkTest> mTarget;

        MainHandler(NetworkTest target) {
            mTarget = new WeakReference<NetworkTest>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            NetworkTest target = mTarget.get();
            if (target != null) {
                target.handleMessage(msg);
            }
        }
    }
}
