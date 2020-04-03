package com.yusong.camtest;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;
import java.util.List;

public class CameraHelper implements Camera.PreviewCallback {

    private static final String TAG = CameraHelper.class.getSimpleName();
    private static final String YUFAN_DEVICE_MODEL = "rk3288";
    private  Activity mActivity;
    private Camera mCamera;
    private int mCameraId;
    private SurfaceHolder mSurfaceHolder;
    private Camera.Size previewSize;
    private int surfaceWidth, surfaceHeight;
    private CameraListener cameraListener;
    //宇松人脸机需要使用后置摄像头
    private boolean frontCamera = true;
    private View previewView;
    private boolean isViewImage;

    public CameraHelper(View view, Activity activity) {
        this.mActivity = activity;
        previewView = view;
        if (view instanceof SurfaceView) {
            this.mSurfaceHolder = ((SurfaceView) view).getHolder();
            this.mSurfaceHolder.addCallback(mPreviewSurfaceCallBack);
        }
//        if (view instanceof TextureView) {
//            ((TextureView) view).setSurfaceTextureListener(textureListener);
//        }
    }

    public void setCameraListener(CameraListener cameraListener) {
        this.cameraListener = cameraListener;
    }

    public void setCameraPos(boolean frontCamera) {
        this.frontCamera = frontCamera;
    }

    public void setCameraImage() {
        if (previewView instanceof TextureView) {
            isViewImage = true;
            previewView.setScaleX(-1);
        }
    }
    int tempOpenCameraTimes = 0;
    public void openCamera() {
        if (mCamera != null) {
            return;
        }

        try {
//            if (frontCamera) {
//                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//            } else {
//                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
//            }
            mCamera = Camera.open(1);
            int cameraOrientation = getCameraOri();
            Log.d("feisher","系统屏幕方向"+cameraOrientation);
//            mCamera.setDisplayOrientation(cameraOrientation);
            mCamera.setDisplayOrientation(90);





            DisplayMetrics metrics = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            surfaceWidth = metrics.widthPixels;
            surfaceHeight = metrics.heightPixels;
            if (mCamera.getParameters() !=null) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                for (String mode : parameters.getSupportedFocusModes()) {
                    if (mode.contains(Camera.Parameters.FOCUS_MODE_AUTO) || mode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(mode);
                    }
                }

                if (parameters.isZoomSupported()) {
                    int maxZoom = parameters.getMaxZoom();
                    Log.d("feisher","相机支持缩放，最大缩放倍数："+maxZoom);

                }else {
                    Log.d("feisher","相机不支持缩放");
                }

                previewSize = get7201280Size(parameters.getSupportedPreviewSizes(), metrics);
//                previewSize = get480640Size(parameters.getSupportedPreviewSizes(), metrics);
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                parameters.setPictureSize(previewSize.width, previewSize.height);
                //todo  处理摄像头镜像问题
//                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    //设置镜像效果，支持的值为flip-mode-values=off,flip-v,flip-h,flip-vh;
//                }
                parameters.set("preview-flip", "flip-h");
                String param = parameters.flatten();
                Log.d("feisehr","相机参数为："+param);
                mCamera.setParameters(parameters);
            }

//        if (previewView instanceof TextureView) {
//            mCamera.setPreviewTexture(((TextureView) previewView).getSurfaceTexture());
//        } else {
//        }
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setPreviewCallback(this);

            mCamera.startPreview();



            if (cameraListener != null) {
                cameraListener.onCameraOpened(previewSize);
            }
            tempOpenCameraTimes = 0;
        } catch (Exception e) {
            e.printStackTrace();
            //打开摄像头失败就再次尝试打开，尝试3次
            if (tempOpenCameraTimes<5){
//                frontCamera = !frontCamera;
                tempOpenCameraTimes++;
                openCamera();
            }else {
            }

        }
    }


    private void stop() {
        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewDisplay(null);
            if (mSurfaceHolder != null) {
                mSurfaceHolder.removeCallback(mPreviewSurfaceCallBack);
                mSurfaceHolder = null;
            }
//            if (textureListener != null) {
//                textureListener = null;
//            }
            mPreviewSurfaceCallBack = null;
            mCamera.release();
            mCamera = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取相机旋转角度
     *
     * @return
     */
    public int getCameraOri() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }
        int result;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int oriADDDegree = 0;
        //2018年8月10日 20点25分  不需要旋转角度
//        if (YUFAN_DEVICE_MODEL.contains(DeviceUtils.getModel())) {
//            //宇帆的设备上需要加90度
//            oriADDDegree = 90;
//        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

            result = (info.orientation + degrees + oriADDDegree) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360 + oriADDDegree) % 360;
        }
        return result;
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public boolean getViewImage() {
        return isViewImage;
    }

    /**
     * 获取最佳比例
     *
     * @param sizes
     * @param metrics
     * @return
     */
    private Camera.Size getMaxSupportedSize(List<Camera.Size> sizes, DisplayMetrics metrics) {
        Camera.Size bestSize = sizes.get(0);
        for (Camera.Size s : sizes) {
            if (s.width > bestSize.width && s.height > bestSize.height){
                bestSize = s;
            }
        }
        return bestSize;
    }

    /**
     * 获取最佳比例
     *
     * @param sizes
     * @param metrics
     * @return
     */
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, DisplayMetrics metrics) {
        Camera.Size bestSize = sizes.get(0);
        float screenRatio = (float) metrics.widthPixels / (float) metrics.heightPixels;
        if (screenRatio > 1) {
            screenRatio = 1 / screenRatio;
        }

        for (Camera.Size s : sizes) {
            if (Math.abs((s.height / (float) s.width) - screenRatio) < Math.abs(bestSize.height /
                    (float) bestSize.width - screenRatio)) {
                bestSize = s;
            }
        }
        return bestSize;
    }

    /**
     * 获取 最佳比例
     *
     * @param sizes
     * @param metrics
     * @return
     */
    private Camera.Size get480640Size(List<Camera.Size> sizes, DisplayMetrics metrics) {
        Camera.Size bestSize = sizes.get(0);
        for (Camera.Size s : sizes) {
            if (s.width > bestSize.width && s.height > bestSize.height){
                bestSize = s;
            }
            if (s.width == 480 && s.height == 640) {
                bestSize = s;
                break;
            }

            if (s.width == 640 && s.height == 480) {
                bestSize = s;
                break;
            }
        }
        return bestSize;
    }
    private Camera.Size get7201280Size(List<Camera.Size> sizes, DisplayMetrics metrics) {
        Camera.Size bestSize = sizes.get(0);
        for (Camera.Size s : sizes) {
            if (s.width > bestSize.width && s.height > bestSize.height){
                bestSize = s;
            }
            if (s.width == 720 && s.height == 1280) {
                bestSize = s;
                break;
            }

            if (s.width == 1280 && s.height == 720) {
                bestSize = s;
                break;
            }
        }
        return bestSize;
    }
    private Camera.Size get7001280Size(List<Camera.Size> sizes, DisplayMetrics metrics) {
        Camera.Size bestSize = sizes.get(0);
        for (Camera.Size s : sizes) {
            if (s.width > bestSize.width && s.height > bestSize.height){
                bestSize = s;
            }
            if (s.width == 700 && s.height == 1280) {
                bestSize = s;
                break;
            }

            if (s.width == 1280 && s.height == 700) {
                bestSize = s;
                break;
            }
        }
        return bestSize;
    }

    //上次记录的时间戳
    long lastRecordTime = System.currentTimeMillis();
    //上次记录的索引
    int darkIndex = 0;
    //一个历史记录的数组，255是代表亮度最大值
    long[] darkList = new long[]{255, 255, 255, 255};
    //扫描间隔
    int waitScanTime = 300;
    //亮度低的阀值
    int darkValue = 128;
    @Override
    public void onPreviewFrame(byte[] nv21, Camera camera) {
        if (cameraListener != null) {
            cameraListener.onNV21Data(nv21, previewSize, surfaceWidth, surfaceHeight);
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRecordTime < waitScanTime) {
            return;
        }
        lastRecordTime = currentTime;

        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;
        //像素点的总亮度
        long pixelLightCount = 0L;
        //像素点的总数
        long pixeCount = width * height;
        //采集步长，因为没有必要每个像素点都采集，可以跨一段采集一个，减少计算负担，必须大于等于1。
        int step = 10;
        //data.length - allCount * 1.5f的目的是判断图像格式是不是YUV420格式，只有是这种格式才相等
        //因为int整形与float浮点直接比较会出问题，所以这么比
        if (Math.abs(nv21.length - pixeCount * 1.5f) < 0.00001f) {
            for (int i = 0; i < pixeCount; i += step) {
                //如果直接加是不行的，因为data[i]记录的是色值并不是数值，byte的范围是+127到—128，
                // 而亮度FFFFFF是11111111是-127，所以这里需要先转为无符号unsigned long参考Byte.toUnsignedLong()
                pixelLightCount += ((long) nv21[i]) & 0xffL;
            }
            //平均亮度
            long cameraLight = pixelLightCount / (pixeCount / step);
            //更新历史记录
            int lightSize = darkList.length;
            darkList[darkIndex = darkIndex % lightSize] = cameraLight;
            darkIndex++;
            boolean isDarkEnv = true;
            //判断在时间范围waitScanTime * lightSize内是不是亮度过暗
            for (int i = 0; i < lightSize; i++) {
                if (darkList[i] > darkValue) {
                    isDarkEnv = false;
                }
            }
            Log.e(TAG, "摄像头环境亮度为 ： " + cameraLight+"》》是否过暗："+isDarkEnv);

        }
    }

//    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//            openCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
//            surfaceWidth = width;
//            surfaceHeight = height;
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//            stop();
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//        }
//    };

    private SurfaceHolder.Callback mPreviewSurfaceCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            surfaceWidth = width;
            surfaceHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stop();
        }
    };

    public interface CameraListener {

        /**
         * 相机打开成功回调
         */
        void onCameraOpened(Camera.Size previewSize);

        /**
         * nv数据回调
         *
         * @param nv21
         * @param previewSize
         * @param surfaceWidth
         * @param surfaceHeight
         */
        void onNV21Data(byte[] nv21, Camera.Size previewSize, int surfaceWidth, int surfaceHeight);
    }
}
