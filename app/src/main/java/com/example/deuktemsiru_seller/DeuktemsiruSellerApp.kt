package com.example.deuktemsiru_seller

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class DeuktemsiruSellerApp : Application() {


    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
