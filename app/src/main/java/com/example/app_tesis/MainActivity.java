package com.example.app_tesis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

import android.os.Environment;
import android.os.StrictMode;
import android.view.Surface;
import android.graphics.Matrix;

import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.lang.String;



import java.io.*;

import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSIONS=101;
    private String[] REQUIRED_PERMISSIONS=new String[]{"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();


        textureView=(TextureView) findViewById(R.id.view_finder);
        
        
        if(allPermisionGranded())
        {
            startCamera();

        }
        else
        {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitNetwork().build());
        ejecutaCliente();
    }

    private void startCamera() {
        CameraX.unbindAll();
        Rational aspectRatio = new Rational(textureView.getWidth(),textureView.getHeight());
        Size screen = new Size(textureView.getWidth(),textureView.getHeight());


        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        final Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {

                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();

                    }
                }
        );

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY).
                setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final  ImageCapture imgCap = new ImageCapture(imageCaptureConfig);
        findViewById(R.id.imgCapture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {





                File file =new File("/storage/emulated/0/hola.dng");

                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        String msg = "Pic save in "+ file.getAbsolutePath();
                        Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                        String msg = "Picture no capturated: " + message;
                        Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();

                        if(cause != null)
                        {
                            cause.printStackTrace();
                        }

                    }
                });


            }
        });

        CameraX.bindToLifecycle(this, preview, imgCap);





    }

    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cx = w / 2f;
        float cy = h / 2f;

        int rotacionDgr;
        int rotation = (int)textureView.getRotation();


        switch (rotation)
        {
            case Surface.ROTATION_0:
                rotacionDgr = 0;
                break;

            case Surface.ROTATION_90:
                rotacionDgr = 90;
                break;

            case Surface.ROTATION_180:
                rotacionDgr = 180;
                break;

            case Surface.ROTATION_270:
                rotacionDgr = 270;
                break;

            default:
                return;
        }

        mx.postRotate((float)rotacionDgr,cx,cy);
        textureView.setTransform(mx);







    }

    private boolean allPermisionGranded() {
        for(String permission: REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }

        return true;
    }

    private void ejecutaCliente(){
        String ip = "192.168.1.88";
        int puerto = 5000;
        log(" socket " + ip + " " + puerto);
        try{
            Socket clientSocket = new Socket(ip,puerto);

            OutputStream outputStream = clientSocket.getOutputStream();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            BufferedImage image = ImageIO.read(new File("C:\\Users\\juan\\Desktop\\recono\\coche.png"));
            ImageIO.write(image, "jpg", byteArrayOutputStream);

            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            System.out.println(byteArrayOutputStream.size());
            outputStream.write(byteArrayOutputStream.toByteArray());
            Thread.sleep(2000);

            outputStream.close();
            clientSocket.close();
        }
        catch (Exception e){
            log("error: " + e.toString());
        }
    }

    private void log(String cadena){



        Toast.makeText(getBaseContext(),cadena,Toast.LENGTH_LONG).show();

    }
}
