package com.otter.otterbenchmark.system;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.otter.otterbenchmark.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Display system information (e.g., SDK, device, build).
 */
public class SystemInfo extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SystemInfo.class.getSimpleName();

    private Button rotate_screen;
    private TextView incremental;
    private TextView codename;
    private TextView release;
    private TextView sdk_int;
    private TextView preview_sdk_int;
    private TextView base_os;
    private TextView security_patch;
    private TextView user;
    private TextView manufacturer;
    private TextView host;
    private TextView brand;
    private TextView product;
    private TextView model;
    private TextView id;
    private TextView time;
    private TextView type;
    private TextView tags;
    private TextView display;
    private TextView fingerprint;
    private TextView device;
    private TextView board;
    private TextView hardware;
    private TextView serial;
    private TextView bootloader;
    private TextView cpu_abi;
    private TextView radio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_system_info);
        findViews();
        rotate_screen.setOnClickListener(this);

        incremental.setText(Build.VERSION.INCREMENTAL);
        codename.setText(Build.VERSION.CODENAME);
        release.setText(Build.VERSION.RELEASE);
        sdk_int.setText(String.valueOf(Build.VERSION.SDK_INT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            preview_sdk_int.setText(String.valueOf(Build.VERSION.PREVIEW_SDK_INT));
            base_os.setText(Build.VERSION.BASE_OS);
            security_patch.setText(Build.VERSION.SECURITY_PATCH);
        } else {
            preview_sdk_int.setText(R.string.system_require_api_23);
            base_os.setText(R.string.system_require_api_23);
            security_patch.setText(R.string.system_require_api_23);
        }

        user.setText(Build.USER);
        manufacturer.setText(Build.MANUFACTURER);
        host.setText(Build.HOST);
        brand.setText(Build.BRAND);
        product.setText(Build.PRODUCT);
        model.setText(Build.MODEL);
        id.setText(Build.ID);
        time.setText(getDateTime(Build.TIME));
        type.setText(Build.TYPE);
        tags.setText(Build.TAGS);
        display.setText(Build.DISPLAY);
        fingerprint.setText(Build.FINGERPRINT);
        device.setText(Build.DEVICE);
        board.setText(Build.BOARD);
        hardware.setText(Build.HARDWARE);
        serial.setText(Build.SERIAL);
        bootloader.setText(Build.BOOTLOADER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cpu_abi.setText(Build.SUPPORTED_ABIS[0]);
        } else {
            cpu_abi.setText(Build.CPU_ABI);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            radio.setText(Build.getRadioVersion());
        } else {
            radio.setText(Build.RADIO);
        }
    }

    private void findViews() {
        rotate_screen = (Button) findViewById(R.id.rotate_screen);
        incremental = (TextView) findViewById(R.id.incremental);
        codename = (TextView) findViewById(R.id.codename);
        release = (TextView) findViewById(R.id.release);
        sdk_int = (TextView) findViewById(R.id.sdk_int);
        preview_sdk_int = (TextView) findViewById(R.id.preview_sdk_int);
        base_os = (TextView) findViewById(R.id.base_os);
        security_patch = (TextView) findViewById(R.id.security_patch);
        user = (TextView) findViewById(R.id.user);
        manufacturer = (TextView) findViewById(R.id.manufacturer);
        host = (TextView) findViewById(R.id.host);
        brand = (TextView) findViewById(R.id.brand);
        product = (TextView) findViewById(R.id.product);
        model = (TextView) findViewById(R.id.model);
        id = (TextView) findViewById(R.id.id);
        time = (TextView) findViewById(R.id.time);
        type = (TextView) findViewById(R.id.type);
        tags = (TextView) findViewById(R.id.tags);
        display = (TextView) findViewById(R.id.display);
        fingerprint = (TextView) findViewById(R.id.fingerprint);
        device = (TextView) findViewById(R.id.device);
        board = (TextView) findViewById(R.id.board);
        hardware = (TextView) findViewById(R.id.hardware);
        serial = (TextView) findViewById(R.id.serial);
        bootloader = (TextView) findViewById(R.id.bootloader);
        cpu_abi = (TextView) findViewById(R.id.cpu_abi);
        radio = (TextView) findViewById(R.id.radio);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rotate_screen:
                switch (getOrientation()) {
                    case Configuration.ORIENTATION_PORTRAIT:
                        setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    case Configuration.ORIENTATION_LANDSCAPE:
                        setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                }

                break;
            default:
                break;
        }
    }

    private int getOrientation () {
        return getResources().getConfiguration().orientation;
    }

    public String getDateTime(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mmZ", Locale.getDefault());
        return sdf.format(new Date(milliseconds));
    }
}
