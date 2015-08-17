package com.otter.otterbenchmark.storage;

import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;

/**
 * Analyze storage speed.
 *
 * Create a temp file to target storage. And then, read it and calculate the speed.
 */
public class StorageTest extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = StorageTest.class.getSimpleName();

    private static final String[] PATH_LIST = {
            Environment.getExternalStorageDirectory().getPath(),
    };

    private static final long UPDATE_FREQUENCY = 1000;

    private MainHandler mMainHandler = new MainHandler(this);
    private static final int MSG_TEST_BEGIN = 0;
    private static final int MSG_TEST_FINISH = 1;
    private static final int MSG_TEST_FAIL = 2;
    private static final int MSG_TEST_CANCEL = 3;
    private static final int MSG_PREPARE_FILE = 8;
    private static final int MSG_STORAGE_NO_PERMISSION = 7;
    private static final int MSG_PREPARE_FILE_FAIL = 4;
    private static final int MSG_DOWNLOADING = 5;
    private static final int MSG_PERIOD_UPDATE = 6;

    private Spinner path_spinner;
    private TextView file_size;
    private TextView elapsed_time;
    private TextView download_speed;
    private TextView status;
    private Button test_start;
    private Button test_cancel;

    private long mFileSize = -1;
    private long mElapsedTime = -1;
    private long mDownloadSpeed = -1;

    private boolean mRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storage_test);
        findViews();
        setSpinner();
        setListener();

        path_spinner.setSelection(0);
    }

    private void findViews() {
        path_spinner = (Spinner) findViewById(R.id.path_spinner);
        file_size = (TextView) findViewById(R.id.file_size);
        elapsed_time = (TextView) findViewById(R.id.elapsed_time);
        download_speed = (TextView) findViewById(R.id.download_speed);
        status = (TextView) findViewById(R.id.status);
        test_start = (Button) findViewById(R.id.test_start);
        test_cancel = (Button) findViewById(R.id.test_cancel);
    }

    private void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, PATH_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        path_spinner.setAdapter(adapter);
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

            // Check permission of target storage.
            File storage = new File(path_spinner.getSelectedItem().toString());
            if (!storage.canRead() || !storage.canWrite()) {
                Log.w(TAG, "No permission to read/write " + storage.getAbsolutePath());
                sendMainHandlerMsg(MSG_STORAGE_NO_PERMISSION);
                return;
            }

            // Prepare 100MB test file.
            sendMainHandlerMsg(MSG_PREPARE_FILE);
            File file  = new File(storage, "100MB.bin");
            if (!prepareTestFile(file, 1024 * 1024 * 100)) {
                sendMainHandlerMsg(MSG_PREPARE_FILE_FAIL);
                return;
            }
            mFileSize = file.length();

            InputStream inputStream = null;

            try {
                // Downloading
                sendMainHandlerMsg(MSG_DOWNLOADING);
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024 * 8];
                long start = System.currentTimeMillis();
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
                Log.w(TAG, "File read failed", e);
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

            file.delete();
        }
    };

    /** Create a new file with specific size. */
    private boolean prepareTestFile(File file, long size) {
        if (file.exists()) {
            if(!file.delete()) {
                Log.w(TAG, file.getAbsolutePath() + " delete failed");
                return false;
            }
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(size);
        } catch (IOException e) {
            Log.w(TAG, file.getAbsolutePath() + " create failed", e);
            return false;
        }

        return file.exists() && (file.length() == size);
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TEST_BEGIN:
                status.setText("Test beginning...");
                test_start.setEnabled(false);

                // Reset information
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
            case MSG_PREPARE_FILE:
                status.setText("Prepare test file...");
                break;
            case MSG_PREPARE_FILE_FAIL:
                status.setText("Prepare test file failed.");
                test_start.setEnabled(true);
                break;
            case MSG_STORAGE_NO_PERMISSION:
                status.setText("No permission to read/write the storage.");
                test_start.setEnabled(true);
                break;
            case MSG_DOWNLOADING:
                status.setText("Downloading...");
                file_size.setText(Util.getSizeString(mFileSize));
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
        private final WeakReference<StorageTest> mTarget;

        MainHandler(StorageTest target) {
            mTarget = new WeakReference<StorageTest>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            StorageTest target = mTarget.get();
            if (target != null) {
                target.handleMessage(msg);
            }
        }
    }
}
