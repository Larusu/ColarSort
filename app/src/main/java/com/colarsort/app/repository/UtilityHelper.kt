package com.colarsort.app.repository

import android.content.Context
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
}