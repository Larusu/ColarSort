package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.colarsort.app.R
import com.colarsort.app.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val handler = Handler(Looper.getMainLooper())
    private var messageIndex = 0
    private val loadingMessages = listOf(
        "Gathering Materials...",
        "Preparing Products...",
        "Checking Inventory...",
        "Finalizing Orders...",
        "Launching Application..."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startLogoAnimation()
        startLoadingTextRotation()
        moveToLoginActivity()
    }

    // Fade-in animation for logo
    private fun startLogoAnimation() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }
        binding.imageView.startAnimation(fadeIn)
    }

    // Show loading messages
    private fun startLoadingTextRotation() {
        handler.post(object : Runnable {
            override fun run() {
                binding.tvLoadingText.text = loadingMessages[messageIndex]
                messageIndex = (messageIndex + 1) % loadingMessages.size
                handler.postDelayed(this, 1500)
            }
        })
    }

    // Go to LoginActivity
    private fun moveToLoginActivity() {
        handler.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 6500) // Splash duration
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
