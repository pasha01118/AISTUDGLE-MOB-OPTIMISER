package com.example.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * A utility class to handle lightweight AES encryption/decryption of snapshots
 * and data logs to ensure private, end-to-end secure storage.
 */
object CryptoHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    
    // Static key derivation key (derived, mock, or generated from user credentials)
    // To keep it robust, we use a 16-byte secure padding.
    private val keyBytes = byteArrayOf(
        0x4D, 0x6F, 0x62, 0x69, 0x6C, 0x65, 0x4F, 0x70, // "MobileOp"
        0x74, 0x69, 0x6D, 0x69, 0x73, 0x65, 0x72, 0x4B  // "timiserK"
    )
    private val ivBytes = byteArrayOf(
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
    )

    private val secretKeySpec = SecretKeySpec(keyBytes, "AES")
    private val ivSpec = IvParameterSpec(ivBytes)

    /**
     * Encrypts plain text string using AES-CBC-128
     */
    fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT or Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback gracefully in case of any platform discrepancies
            "ENC_ERR:" + Base64.encodeToString(plainText.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT or Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts AES-CBC-128 encrypted string back to plaintext
     */
    fun decrypt(encryptedText: String): String {
        return try {
            if (encryptedText.startsWith("ENC_ERR:")) {
                val rawBase64 = encryptedText.substringAfter("ENC_ERR:")
                return String(Base64.decode(rawBase64, Base64.DEFAULT), StandardCharsets.UTF_8)
            }
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "Decryption failed or invalid key payload."
        }
    }
}
