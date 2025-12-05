package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import com.colarsort.app.databinding.ActivitySplashScreenBinding
import com.colarsort.app.data.repository.MaterialsRepo
import com.colarsort.app.data.repository.ProductMaterialsRepo
import com.colarsort.app.data.repository.ProductsRepo
import com.colarsort.app.data.repository.RepositoryProvider

@Suppress("CustomSplashScreen")
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
    private lateinit var materialsRepo : MaterialsRepo
    private lateinit var productsRepo : ProductsRepo
    private lateinit var productMaterialsRepo : ProductMaterialsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        materialsRepo = RepositoryProvider.materialsRepo
        productsRepo = RepositoryProvider.productsRepo
        productMaterialsRepo = RepositoryProvider.productMaterialsRepo

        startLogoAnimation()
        startLoadingTextRotation()
    }

    override fun onStart() {
        super.onStart()

        if (sessionManager.isLoggedIn()) {
            moveToHomeActivity()
        } else {
            moveToLoginActivity()
        }
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
    
    private fun moveToHomeActivity()
    {
        handler.postDelayed( {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, 6500)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
