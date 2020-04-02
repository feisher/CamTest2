package com.yusong.camtest

import android.annotation.SuppressLint
import android.content.Context

/**
 * Description: 统一配置扩展方法中的变量
 * Create by lxj, at 2018/12/4
 */
@SuppressLint("StaticFieldLeak")
object KtxConfig {

    lateinit var context: Context
    var isDebug = true
    var defaultLogTag = "OkGoHttp"
    var sharedPrefName = "faceSp"

    /**
     * 初始化配置信息，必须调用
     * @param isDebug 是否是debug模式，默认为true
     */
    fun init(context: Context) {
        this.context = context
        this.isDebug = isDebug
        this.defaultLogTag = defaultLogTag
        this.sharedPrefName = sharedPrefName
//        ISNav.getInstance().init { _, path, imageView -> imageView?.load(path) }
//        DirManager.init(context)


    }


}