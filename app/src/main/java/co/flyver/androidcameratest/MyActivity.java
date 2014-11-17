package co.flyver.androidcameratest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;



public class MyActivity extends Activity implements Session.Callback, SurfaceHolder.Callback {
    private Camera cameraObject;
    private ShowCamera showCamera;
    private ImageView pic;
    private Button takePicture;
    private int picsTaken = 0;
    private Session mSession;
    private net.majorkernelpanic.streaming.gl.SurfaceView mSurfaceView;
    private final String TAG = "CameraTest";


    public static Camera isCameraAvailiable(){
        Camera object = null;
        try {
            object = Camera.open();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return object;
    }

    private Camera.PictureCallback capturedIt = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if(bitmap==null){
//                Toast.makeText(getApplicationContext(), "not taken", Toast.LENGTH_SHORT).show();
            }
            else
            {
//                Toast.makeText(getApplicationContext(), "taken", Toast.LENGTH_SHORT).show();
                Log.d("CAMERA", "Picture taken count: " + picsTaken);
                Log.d("CAMERA", "Picture data length: " + data.length / 1024 + "kB");
                Log.d("CAMERA", "Picture data: " + Arrays.toString(data));
                pic.setImageBitmap(bitmap);
                camera.startPreview();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            picsTaken++;
            snapIt(findViewById(R.id.imageView1));

//            cameraObject.release();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
//        pic = (ImageView)findViewById(R.id.imageView1);
//        cameraObject = isCameraAvailiable();
//        showCamera = new ShowCamera(this, cameraObject);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//        takePicture = (Button) findViewById(R.id.button1);
//        takePicture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                picsTaken = 0;
//                snapIt(v);
//            }
//        });
//        preview.addView(showCamera);
//        listUsb();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, String.valueOf(54321));
        editor.apply();

        mSurfaceView = (net.majorkernelpanic.streaming.gl.SurfaceView) findViewById(R.id.camera_preview);
        mSession = SessionBuilder.getInstance()
                .setCallback(this)
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(800,480,5,900000))
                .build();

        mSurfaceView.getHolder().addCallback(this);

        this.startService(new Intent(this,RtspServer.class));
    }
    public void snapIt(View view){
        if(picsTaken <= 10) {
            cameraObject.takePicture(null, null, capturedIt);
        }
    }

    public void listUsb() {
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        UsbDevice device;
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
//            Log.d("TEST","Name: " + device.getDeviceName());
            Toast.makeText(getApplicationContext(), device.getDeviceName(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.d(TAG, "Bitrate updated " + bitrate);

    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {

    }

    @Override
    public void onPreviewStarted() {
        Log.d(TAG, "Preview started");

    }

    @Override
    public void onSessionConfigured() {
        Log.d(TAG, "Session configured");
        mSession.start();
    }

    @Override
    public void onSessionStarted() {

        Log.d(TAG, "Session started");
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG, "Session stopped");

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        mSession.startPreview();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSession.stop();

    }
}
