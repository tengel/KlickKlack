package org.tengel.klickklack;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.app.AlertDialog;
import 	android.widget.FrameLayout;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import java.util.List;
import android.os.Environment;
import android.net.Uri;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.content.Context;
import android.os.Handler;
import android.widget.TextView;
import android.content.DialogInterface;
import android.widget.EditText;
import java.lang.Integer;
import android.hardware.SensorManager;
import android.hardware.Sensor;


public class KlickKlack extends Activity
{
    public static final String TAG = "KlickKlack";

    private static Camera  m_camera;
    private CameraPreview  m_preview;
    private PictureHandler m_fileHandler;
    private Handler        m_timerHandler = new Handler();
    private int            m_interval = 3;
    private boolean        m_isStarted = false;
    private int            m_timeout = 3;
    private int            m_picCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        try
        {
            setupApp();

            Runnable updateUiTask = new Runnable()
            {
                public void run()
                {
                    if (m_isStarted)
                    {
                        m_timeout -= 1;
                        if (m_timeout <= 0)
                        {
                            m_timeout = m_interval;
                            KlickKlack.m_camera.takePicture(null, null, m_fileHandler);
                            m_picCount += 1;
                        }
                    }
                    TextView tv = (TextView) findViewById(R.id.text_info);
                    tv.setText("Running:" + m_isStarted +
                               " Count:" + m_picCount +
                               " Interval:" + m_interval +
                               " Timeout:" + m_timeout +
                               " File:" + m_fileHandler.getPictureFile());
                    m_timerHandler.postDelayed(this, 1000);
                }
            };
            m_timerHandler.postDelayed(updateUiTask, 1000);
        }
        catch (Exception e)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("onCreate: " + e.toString() + " " +
                               e.getStackTrace()[0].toString());
            builder.create().show();
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }


    private void setupApp()
    {
        KlickKlack.m_camera = Camera.open();
        Camera.Parameters parameter = m_camera.getParameters();
        Log.d(TAG, "supported formats: " + parameter.getSupportedPictureFormats());
        parameter.setPictureFormat(ImageFormat.JPEG);
        parameter.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameter.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        m_camera.setParameters(parameter);

        m_preview = new CameraPreview(this, KlickKlack.m_camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(m_preview);
        m_fileHandler = new PictureHandler(m_preview, (Context)this);
        Log.d(TAG, "setupApp()");

        SensorManager sensorManager;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for( Sensor s: deviceSensors) {
            Log.d(TAG, "Sensor: Vendor: '" + s.getVendor() + "' Name: '" + s.getName() +
                    "' Type: " + s.getType());
        }

        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            Log.d(TAG, "no light sensor found");
        }


    }


    public void onCapture(View view)
    {
        KlickKlack.m_camera.takePicture(null, null, m_fileHandler);
    }


    public void onStartStop(View view)
    {
        Button b = (Button) findViewById(R.id.button_startStop);
        if (m_isStarted)
        {
            m_isStarted = false;
            b.setText("Start");
            m_timeout = m_interval;
        }
        else
        {
            m_isStarted = true;
            b.setText("Stop");
            m_picCount = 0;
        }
    }


    public void onSetTime(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter interval in seconds");
        final EditText input = new EditText(this);
        input.setText(Integer.toString(m_interval));
        builder.setView(input);
        builder.setPositiveButton(
            "ok",
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    m_interval = Integer.parseInt(input.getText().toString());
                    m_timeout = m_interval;
                }
            });
        builder.create().show();
    }


    public void onExit(View view)
    {
        m_isStarted = false;
        if (KlickKlack.m_camera != null)
        {
            KlickKlack.m_camera.release();
            KlickKlack.m_camera = null;
            m_preview = null;
        }
        finish();
    }

    public void onInfo(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("KlickKlack");
        builder.create().show();
    }


}
