package org.tengel.klickklack;

import java.io.IOException;
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


class PictureHandler implements PictureCallback
{
    private static final String TAG = KlickKlack.TAG;
    
    private CameraPreview m_preview;
    private File          m_dir;
    private Context       m_context;
    private File          m_pictureFile;


    
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
            return m_pictureFile.getName();
        }
        else
        {
            return " - ";
        }
    }
    
    
}
