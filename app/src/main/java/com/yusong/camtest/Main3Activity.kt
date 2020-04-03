package com.yusong.camtest

import android.graphics.*
import android.hardware.Camera
import android.media.FaceDetector
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.yusong.ToastUtils
import com.yusong.camtest.Ir.CameraListener
import com.yusong.camtest.Ir.IrCameraHelper
import kotlinx.android.synthetic.main.activity_main3.*
import java.io.ByteArrayOutputStream
import java.io.IOException


/*
* @author feisher
* @emil 458079442@qq.com
* create at 2020-04-03
* description: 红外摄像头帮助类测试
*/
class Main3Activity : AppCompatActivity() {
    private var bitmap: Bitmap? = null
    private var faces1 = 0
    var irCameraHelper: IrCameraHelper? = null
    private var fastYUVtoRGB: FastYUVtoRGB? = null
    var baos: ByteArrayOutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        fastYUVtoRGB =  FastYUVtoRGB(this)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true //加载的时候只加载图片的宽高属性，不加载原图
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inSampleSize = 4
        options.inDither = true
        options.inJustDecodeBounds = false //取消加载时只加载宽高
        val m = Matrix()
        m.postRotate(90f)
        val faces = arrayOfNulls<FaceDetector.Face>(1)

        irCameraHelper = IrCameraHelper.Builder()
                .previewViewSize(Point(720, 1280))
//                .rotation(windowManager.defaultDisplay.rotation)
                .rotation(0)
                .specificCameraId(1)
                .isMirror(false)
                .previewOn(surfaceView)
                .cameraListener(object : CameraListener{
                    override fun onPreview(nv21: ByteArray?, camera: Camera?) {
                        try {
//                            val timings = TimingLogger("TAG", "onNV21Data")
//                            var nv21ToBitmap = nv21ToBitmap(nv21, 1280, 720)
                            val nv21ToBitmap = fastYUVtoRGB?.convertYUVtoRGB(nv21, 1280, 720)
//                            timings.addSplit("解析图片")

                            baos = ByteArrayOutputStream()
                            nv21ToBitmap?.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                            val toByteArray = baos?.toByteArray()
                            val bmNew = BitmapFactory.decodeByteArray(toByteArray, 0,toByteArray?.size!!, options)
                            bmNew.copy(Bitmap.Config.RGB_565, true)
//                            timings.addSplit("压缩图片")
                            bitmap = Bitmap.createBitmap(bmNew, 0, 0, bmNew.width, bmNew.height, m, false)
//                            timings.addSplit("旋转图片")
                            bmNew.recycle()
                            baos?.close()


//                            val headBmp = Bitmap.createBitmap(720, 1280, Bitmap.Config.RGB_565)
//                            var code  = ArcSoftImageUtil.imageDataToBitmap(toByteArray, headBmp, ArcSoftImageFormat.NV21)

//                            val rotateHeadImageData = ByteArray(nv21!!.size)
////                            val rotation = windowManager.defaultDisplay.rotation
//                            //获取到的rotation  = 270
//                            ArcSoftImageUtil.rotateImage(nv21, rotateHeadImageData,720,  1280, ArcSoftRotateDegree.DEGREE_180, ArcSoftImageFormat.NV21)
//                            timings.addSplit("旋转图片")
                            //宽高需要互换
//                            var previewSize = camera?.parameters?.previewSize
//                            val headBmp = Bitmap.createBitmap(previewSize?.height!!, previewSize?.width, Bitmap.Config.RGB_565)
//                            var code  = ArcSoftImageUtil.imageDataToBitmap(nv21, headBmp, ArcSoftImageFormat.NV21)
//
//                            timings.addSplit("解析图片")
//                            bitmap = createScaleBitmap(headBmp, 240, 320)
//                            timings.addSplit("压缩图片")
                            //保存文件相关代码

                            val faceDetector = FaceDetector(bitmap?.width!!, bitmap?.height!!, 1)
//                            timings.addSplit("创建解析器")
                            faces1 = faceDetector.findFaces(bitmap, faces)
//                            timings.addSplit("解析人脸")
                            Log.i("feisher", "发现人脸数量 : $faces1")
                            ToastUtils.showShort("发现人脸：$faces1 ")
                            bitmap?.recycle()

//                            timings.dumpToLog() //输出到日志
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            Log.e("feisher", "异常 : " + e.message)
                        }
                    }
                    /**
                     * 当出现异常时执行
                     * @param e 相机相关异常
                     */
                    override fun onCameraError(e: Exception?) {

                    }

                    /**
                     * 属性变化时调用
                     * @param cameraID  相机ID
                     * @param displayOrientation    相机旋转方向
                     */
                    override fun onCameraConfigurationChanged(cameraID: Int, displayOrientation: Int) {

                    }

                    /**
                     * 当打开时执行
                     * @param camera 相机实例
                     * @param cameraId 相机ID
                     * @param displayOrientation 相机预览旋转角度
                     * @param isMirror 是否镜像显示
                     */
                    override fun onCameraOpened(camera: Camera?, cameraId: Int, displayOrientation: Int, isMirror: Boolean) {

                    }

                    /**
                     * 当相机关闭时执行
                     */
                    override fun onCameraClosed() {

                    }


                }).build()

        irCameraHelper?.init()
        irCameraHelper?.start()

    }

    override fun onResume() {
        super.onResume()
        surfaceView.visibility = View.VISIBLE
        irCameraHelper?.start()
        var measuredWidth = surfaceView.measuredWidth
        var measuredHeight = surfaceView.measuredHeight
        Log.i("feisher", "测量宽高 measuredWidth: $measuredWidth measuredHeight：$measuredHeight")
    }

    private fun createScaleBitmap(tempBitmap: Bitmap?, desiredWidth: Int, desiredHeight: Int): Bitmap? {
        return if (tempBitmap != null && (tempBitmap.width > desiredWidth || tempBitmap.height > desiredHeight)) {
            val bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true)
            tempBitmap.recycle() // 释放Bitmap的native像素数组
            bitmap
        } else {
            tempBitmap // 如果没有缩放，那么不回收
        }
    }

    fun nv21ToBitmap(nv21: ByteArray?, width: Int, height: Int): Bitmap? { //        Bitmap bitmap = null;
        try {
            val image = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, width, height), 100, stream)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true //加载的时候只加载图片的宽高属性，不加载原图
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inSampleSize = 6
            options.inDither = true
            options.inJustDecodeBounds = false //取消加载时只加载宽高
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size(), options)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }
    fun rotateBitmap(bitmap: Bitmap?, degress: Int): Bitmap? {
        var bitmap = bitmap
        if (bitmap != null) {
            val m = Matrix()
            m.postRotate(degress.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            return bitmap
        }
        return bitmap
    }
}