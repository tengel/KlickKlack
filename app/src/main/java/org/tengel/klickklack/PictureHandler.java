package org.tengel.klickklack;

import java.io.IOException;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.media.MediaScannerConnection;
import android.content.Context;
import android.graphics.Bitmap;


class PictureHandler implements PictureCallback
{
    private static final String TAG = KlickKlack.TAG;
    
    private CameraPreview m_preview;
    private File          m_dir;
    private Context       m_context;
    private File          m_pictureFile;
    private int           m_grayLevel;
    private String        m_status;


    
    public PictureHandler(CameraPreview cp, Context ctx)
    {
        m_preview = cp;
        m_context = ctx;
        m_dir = new File(Environment.getExternalStorageDirectory(),
                         "KlickKlack");
        if (! m_dir.exists())
        {
            if (! m_dir.mkdirs())
            {
                Log.d(TAG, "failed to create directory");
            }
        }
    }

    
    public void onPictureTaken(byte[] data, Camera camera)
    {
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bm != null) {
            long pxSum = 0;
            int pxCount = 0;
            for (int x = 0; x < bm.getWidth(); x+=100) {
                for (int y = 0; y < bm.getHeight(); y+=100) {
                    int px = bm.getPixel(x, y);
                    pxSum += (Color.red(px) + Color.green(px) + Color.blue(px)) / 3;
                    pxCount += 1;
                }
            }
            m_grayLevel = (int) (pxSum / pxCount);
            Log.d(TAG, "picture gray level: " + m_grayLevel);
        }
        else {
            Log.d(TAG, "Failed to decode byte stream");
        }

        if (m_grayLevel < 50) {
            m_status = "skipped";
            return;
        }
        else {
            m_status = "ok";
        }


        m_preview.restart();
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        m_pictureFile = new File(m_dir, "IMG_" + ts + ".jpg");
        Log.d(TAG, "save: " + m_pictureFile);
        if (m_pictureFile == null)
        {
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }
        try
        {
            FileOutputStream fos = new FileOutputStream(m_pictureFile);
            fos.write(data);
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "File not found: " + e.getMessage());
        }
        catch (IOException e)
        {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        MediaScannerConnection.scanFile(m_context,
                                        new String[] {m_pictureFile.toString()},
                                        null, null);
    }


    String getPictureFile()
    {
        if (m_pictureFile != null)
        {
            return m_pictureFile.getName() + " (" + m_grayLevel + ", " + m_status + ")";
        }
        else
        {
            return " - (" + m_grayLevel + ", " + m_status + ")";
        }
    }
    
    
}
