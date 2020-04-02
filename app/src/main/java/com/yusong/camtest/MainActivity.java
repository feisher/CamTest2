package com.yusong.camtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.arcsoft.imageutil.ArcSoftRotateDegree;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yusong.ToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements CameraHelper.CameraListener {

    private SurfaceView surfaceView;
    private SurfaceView surfaceView2;
    private CameraHelper mCameraHelper;
    private static Bitmap bitmap;
    private int faces1;
    private FastYUVtoRGB fastYUVtoRGB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
//        surfaceView2 = findViewById(R.id.surfaceView2);
//        surfaceView2.setZOrderMediaOverlay(true);
//        surfaceView2.getHolder().setFormat(PixelFormat.RGB_565);
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        mCameraHelper = new CameraHelper(surfaceView,MainActivity.this);
        mCameraHelper.setCameraListener(MainActivity.this);
//        new RxPermissions(this)
//                .request(Manifest.permission.CAMERA)
//                .subscribe(new Consumer<Boolean>() {
//                    @Override
//                    public void accept(Boolean granted) throws Exception {
//                        if (granted) { // Always true pre-M
//                            //相机捕捉图像
//                            mCameraHelper = new CameraHelper(surfaceView,MainActivity.this);
//                            mCameraHelper.setCameraListener(MainActivity.this);
//                        }
//                    }
//                });
//        surfaceView2.setZOrderOnTop(true);
//        CameraHelper2 cameraHelper = new CameraHelper2(surfaceView2, MainActivity.this);
//        cameraHelper.setCameraListener(new CameraHelper2.CameraListener() {
//            @Override
//            public void onCameraOpened(Camera.Size previewSize) {
//
//            }
//
//            @Override
//            public void onNV21Data(byte[] nv21, Camera.Size previewSize, int surfaceWidth, int surfaceHeight) {
//
//            }
//        });
        fastYUVtoRGB = new FastYUVtoRGB(this);
    }

    /**
     * 相机打开成功回调
     *
     * @param previewSize
     */
    @Override
    public void onCameraOpened(Camera.Size previewSize) {
        // 0.2*5 * 3
    }

    /**
     * nv数据回调
     *
     * @param nv21
     * @param previewSize
     * @param surfaceWidth
     * @param surfaceHeight
     */
    @Override
    public void onNV21Data(byte[] nv21, Camera.Size previewSize, int surfaceWidth, int surfaceHeight) {
        try {

            byte[]  rotateHeadImageData = new byte[nv21.length];
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //获取到的rotation  = 270
            int rotateCode = ArcSoftImageUtil.rotateImage(nv21, rotateHeadImageData, previewSize.width, previewSize.height, ArcSoftRotateDegree.DEGREE_270,  ArcSoftImageFormat.NV21);
            if (rotateCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                throw new RuntimeException("rotate image failed, code is " + rotateCode);
            }
            //宽高需要互换
            Bitmap headBmp = Bitmap.createBitmap(previewSize.height,  previewSize.width, Bitmap.Config.RGB_565);
            if (ArcSoftImageUtil.imageDataToBitmap(rotateHeadImageData, headBmp, ArcSoftImageFormat.NV21) != ArcSoftImageUtilError.CODE_SUCCESS) {
                throw new RuntimeException("failed to transform image data to bitmap");
            }
            bitmap = headBmp;
            //保存文件相关代码
//            File file = new File(imgDir + File.separator + "1.jpg");
//            FileOutputStream fosImage = new FileOutputStream(file);
//            headBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
//            fosImage.close();

//            Bitmap bitmap0 = fastYUVtoRGB.convertYUVtoRGB(nv21, previewSize.width, previewSize.height);
//            bitmap = rotateBitmap(bitmap0,90);
//            bitmap = nv21ToBitmap(nv21,  previewSize.width, previewSize.height);
//            MainActivity.bitmap = nv21ToBitmap(nv21,  previewSize.width, previewSize.height, new Rect(0, 0, previewSize.width, previewSize.height));
            FaceDetector.Face[] faces = new FaceDetector.Face[1];
            FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(),  bitmap.getHeight(),1);
            faces1 = faceDetector.findFaces(bitmap, faces);
            Log.i("feisher","发现人脸数量 : " + faces1);
            boolean b = faces1 > 0;
            ToastUtils.showShort("发现人脸："+b);
            bitmap.recycle();
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("feisher","异常 : " + e.getMessage());
        }

    }

    public static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
//        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; //加载的时候只加载图片的宽高属性，不加载原图
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 4;
            options.inDither = true;
            options.inJustDecodeBounds = false;//取消加载时只加载宽高
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size(), options);
            bitmap = rotateBitmap(bitmap,90);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

            return bitmap;
        }
        return bitmap;

    }


}
