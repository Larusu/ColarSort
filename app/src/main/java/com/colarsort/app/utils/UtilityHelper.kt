package com.colarsort.app.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

object UtilityHelper {
    /**
     * Reads an image file from the 'assets' directory and returns as ByteArray
     * It is useful to store image in the Products data class that expects ByteArray
     *
     * @param context - the context use to access the assets folder
     * @param image - the filename of the image in assets directory
     *
     * Example:
     * val bytes = inputStreamToByteArray(this, "Sample.png")
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
}