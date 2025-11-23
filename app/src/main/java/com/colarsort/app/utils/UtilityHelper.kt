package com.colarsort.app.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.scale
import com.colarsort.app.R
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

object UtilityHelper {
    /**
     * Reads an image file from the 'assets' directory and returns as ByteArray
     * It is useful to store image in the Products data class that expects ByteArray
     *
     * @param context The context used to access the assets folder.
     * @param image The filename of the image in the assets directory.
     *
     * ### Usage example:
     *  ```
     *  val bytes: ByteArray = inputStreamToByteArray(this, "Sample.png")
     *  ```
     */
    fun inputStreamToByteArray(context: Context, image: String) : ByteArray
    {
        val inputStream = context.assets.open(image)
        val imageBytes = inputStream.readBytes()
        inputStream.close()

        return imageBytes
    }

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
     * Compresses a bitmap into a JPEG byte array after scaling it to 500x500 pixels.
     *
     * The bitmap is first resized, then compressed at 70% quality, and the result is
     * returned as a byte array suitable for storage or database insertion
     *
     * @param bitmap The original bitmap to compress
     * @return a JPEG-Compressed byte array of the scaled bitmap
     */
    fun compressBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val scaled = bitmap.scale(500, 500)

        scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

        return outputStream.toByteArray()
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


