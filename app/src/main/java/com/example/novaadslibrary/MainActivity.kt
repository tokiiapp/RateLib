package com.example.novaadslibrary

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdValue
import com.rate.ratedialog.RatingDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setContentView(R.layout.activity_main)
        showRate()
        val tv_rate = findViewById<TextView>(R.id.tv_rate)
        tv_rate.setOnClickListener {
            showRate()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    fun showRate() {
        val ratingDialog = RatingDialog.Builder(this)
            .session(1)
            .date(1)
            .setNameApp(getString(R.string.app_name))
            .setIcon(R.mipmap.ic_launcher)
            .setEmail("scasoftwaresp@gmail.com")
            .isShowButtonLater(true)
            .isClickLaterDismiss(true)
            .setOnlickRate {
            }
            .setTextButtonLater("Maybe later")
            .setOnlickMaybeLate { }
            .ratingButtonColor(com.rate.ratedialog.R.color.gnt_blue)
            .build()

        //Cancel On Touch Outside
        ratingDialog.setCanceledOnTouchOutside(true)
        //show
        ratingDialog.show()
    }

}