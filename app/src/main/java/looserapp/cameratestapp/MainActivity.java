package looserapp.cameratestapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    Button captureButton;
    static boolean isTimerStarted = false;
    static int prevLum = 0;
    static int cntPicutres = 0;
    static int lightLum = 0;
    private SurfaceTextureManager mStManager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    long startTime = 0;

    /**
     * Called when the activity is first created.
     */
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
          //  long millis = System.currentTimeMillis() - startTime;
           // int seconds = (int) (millis / 1000);
          //  int minutes = seconds / 60;
          //  seconds = seconds % 60;

            mCamera.takePicture(null, null, mPicture);

            timerHandler.postDelayed(timerRunnable, 900);


        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCamera = getCameraInstance();
        //mCameraPreview = new CameraPreview(this, mCamera);


        SurfaceView dummy=new SurfaceView(this);
        try {
            mCamera.setPreviewDisplay(dummy.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();

        mStManager = new SurfaceTextureManager();
        SurfaceTexture st = mStManager.getSurfaceTexture();
        try {
            mCamera.setPreviewTexture(st);
        } catch (IOException ioe) {
            throw new RuntimeException("setPreviewTexture failed", ioe);
        }

        //mCameraPreview = new CameraPreview(this, mCamera);
        //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
       // preview.addView(mCameraPreview);

        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mCamera.takePicture(null, null, mPicture);
                if( !isTimerStarted )
                {
                    timerRunnable.run();
                    isTimerStarted = true;
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
            Camera.Parameters param=camera.getParameters();
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            param.set("rawsave-mode", "1");
            param.setPictureFormat(ImageFormat.RGB_565);
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            if ((bm.getWidth() > 200) && (bm.getHeight() > 200))
            {
                int lum = Color.red(bm.getPixel(50, 50));
                lum += Color.red(bm.getPixel(100, 100));
                lum += Color.red(bm.getPixel(150, 150));
                lum += Color.red(bm.getPixel(200, 200));
                lum += Color.green(bm.getPixel(50, 50));
                lum += Color.green(bm.getPixel(100, 100));
                lum += Color.green(bm.getPixel(150, 150));
                lum += Color.green(bm.getPixel(200, 200));
                lum += Color.blue(bm.getPixel(50, 50));
                lum += Color.blue(bm.getPixel(100, 100));
                lum += Color.blue(bm.getPixel(150, 150));
                lum += Color.blue(bm.getPixel(200, 200));
                lum = lum/12;

                if( cntPicutres < 10 )
                {
                    lightLum += lum;
                    cntPicutres++;
                }
                else
                {
                    if( lum < ( (lightLum * 5)/100)  )
                    {
                        Log.d("MyCameraApp", "dark");
                        captureButton.setText("Looser detected!");
                        captureButton.setBackgroundColor(Color.RED);
                    }
                    else
                    {
                        Log.d("MyCameraApp", "ligh");
                        captureButton.setText("Searching...");
                        captureButton.setBackgroundColor(Color.GREEN);
                    }
                }

                //camready = true;
            }
        }
    };

    private static File getOutputMediaFile() {

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        //mediaStorageDir.getPath()
        mediaFile = new File( mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://looserapp.cameratestapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://looserapp.cameratestapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
        captureButton.setText("Start looser detection!");
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        //timerHandler.removeCallbacks(timerRunnable);
    }
}

