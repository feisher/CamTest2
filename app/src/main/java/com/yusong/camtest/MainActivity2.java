package com.yusong.camtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.yusong.camtest.camera2.AutoFitTextureView;
import com.yusong.camtest.camera2.Camera2Helper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity2 extends AppCompatActivity implements Camera2Helper.AfterDoListener {

    private Camera2Helper camera2Helper;
    private File file;
    private AutoFitTextureView textureView;
    private ImageView imageView;
    private Button button;
    private ProgressBar progressBar;
    public static final String PHOTO_PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String PHOTO_NAME = "camera2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        EventBus.getDefault().register(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera2Helper.startCameraPreView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        camera2Helper.onDestroyHelper();
    }

    private void init(){
        textureView= (AutoFitTextureView) findViewById(R.id.texture);
        imageView= (ImageView) findViewById(R.id.imv_photo);
        button= (Button) findViewById(R.id.btn_take_photo);
        progressBar= (ProgressBar) findViewById(R.id.progressbar_loading);
        file = new File(PHOTO_PATH, PHOTO_NAME + ".jpg");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera2Helper.takePicture();

            }
        });
        camera2Helper=Camera2Helper.getInstance(MainActivity2.this,textureView,file);
        camera2Helper.setAfterDoListener(this);
    }


    @Override
    public void onAfterPreviewBack() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private Bitmap bitmap;

    @Override
    public void onAfterTakePicture() {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            byte[] byt = new byte[input.available()];
            input.read(byt);
            bitmap = BitmapFactory.decodeByteArray(byt, 0, byt.length);
            EventBus.getDefault().post("dd");


            Log.d("feisher","是否是主线程："+isInMainThread());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventTakePicture(String s){
        imageView.setImageBitmap(bitmap);
    }

    public static boolean isInMainThread() {
        Looper myLooper = Looper.myLooper();
        Looper mainLooper = Looper.getMainLooper();
        Log.i("feisher", "isInMainThread myLooper=" + myLooper + ";mainLooper=" + mainLooper);
        return myLooper == mainLooper;
    }

}