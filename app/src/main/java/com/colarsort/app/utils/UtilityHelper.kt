@file:Suppress("DEPRECATION")

package com.colarsort.app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.colarsort.app.R
import java.io.File
import java.security.MessageDigest

object UtilityHelper {
    /**
     * Generates a SHA-256 hash of a password.
     *
     * The password is converted to bytes processed using the SHA-256
     * MessageDigest algorithm, and the resulting byte array is encoded
     * as a lowercase string.
     *
     * @param password The plain-text password to hash
     * @return A SHA-256 hash represented as a hex string
     */
    fun hashPassword(password: String) : String
    {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Compresses a bitmap into a JPEG file and saves it in the app's internal storage.
     *
     * The image is stored inside the `/files/images` directory using a timestamp-based
     * filename. The bitmap is compressed at 70% JPEG quality and written to disk.
     *
     * @param context The application context used to access internal storage.
     * @param bitmap The bitmap image to compress and save.
     * @return The absolute file path of the saved compressed image.
     */
    fun compressBitmap(context: Context, bitmap: Bitmap): String
    {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdir()

        val file = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")

        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it)
        }

        return file.absolutePath
    }

    /**
     * Show a custom Toast message in any activity.
     *
     * @param activity The activity context
     * @param message The message to display
     * @param duration Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     * @param textColor Color resource ID for text (default: white)
     * @param textSizeSp Text size in sp (default: 14f)
     */
    @SuppressLint("InflateParams")
    fun showCustomToast(
        activity: Activity,
        message: String,
        duration: Int = Toast.LENGTH_SHORT,
        textColor: Int = android.R.color.white,
        textSizeSp: Float = 14f
    ) {
        val inflater = LayoutInflater.from(activity)
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message
        textView.setTextColor(activity.resources.getColor(textColor, activity.theme))
        textView.textSize = textSizeSp

        Toast(activity).apply {
            this.duration = duration
            this.view = layout
            show()
        }
    }
}


