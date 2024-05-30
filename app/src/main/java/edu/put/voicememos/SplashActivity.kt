package edu.put.voicememos

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.logo)
        val appName: TextView = findViewById(R.id.appName)

        // Animacja skali logo
        val scaleXLogo = ObjectAnimator.ofFloat(logo, "scaleX", 0.5f, 1.2f, 1.0f)
        scaleXLogo.duration = 2000
        scaleXLogo.interpolator = BounceInterpolator()

        val scaleYLogo = ObjectAnimator.ofFloat(logo, "scaleY", 0.5f, 1.2f, 1.0f)
        scaleYLogo.duration = 2000
        scaleYLogo.interpolator = BounceInterpolator()

        // Animacja skali napisu
        val scaleXText = ObjectAnimator.ofFloat(appName, "scaleX", 0.5f, 1.2f, 1.0f)
        scaleXText.duration = 2000
        scaleXText.interpolator = BounceInterpolator()

        val scaleYText = ObjectAnimator.ofFloat(appName, "scaleY", 0.5f, 1.2f, 1.0f)
        scaleYText.duration = 2000
        scaleYText.interpolator = BounceInterpolator()

        // Połącz animacje w zestawy
        val logoSet = AnimatorSet()
        logoSet.playTogether(scaleXLogo, scaleYLogo)

        val textSet = AnimatorSet()
        textSet.playTogether(scaleXText, scaleYText)

        // Rozpocznij animacje
        logoSet.start()
        textSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
