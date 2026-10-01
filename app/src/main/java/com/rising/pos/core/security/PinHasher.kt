package com.rising.pos.core.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashing & verifikasi PIN Owner memakai PBKDF2-HMAC-SHA256.
 *
 * PIN TIDAK boleh disimpan sebagai plaintext. Sebelumnya `BusinessSettings.securityPin`
 * menyimpan PIN apa adanya dan `SecurityPinDialog` membandingkannya dengan `==`, sehingga
 * siapa pun yang dapat membaca DataStore (mis. lewat backup/root) langsung mengetahui PIN,
 * padahal PIN ini menggerbangi Void transaksi dan Pemulihan database.
 *
 * Format penyimpanan: hash dan salt terpisah, keduanya Base64. Salt acak 16 byte per PIN,
 * iterasi tinggi membuat brute-force PIN 4-6 digit menjadi mahal.
 */
object PinHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    /** Salt acak baru, di-encode Base64 (NO_WRAP) untuk disimpan di DataStore. */
    fun newSalt(): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        return Base64.getEncoder().withoutPadding().encodeToString(salt)
    }

    /** Hash [pin] dengan [saltBase64]; mengembalikan hash Base64 (NO_WRAP). */
    fun hash(pin: String, saltBase64: String): String {
        val salt = Base64.getDecoder().decode(saltBase64)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val encoded = factory.generateSecret(spec).encoded
        return Base64.getEncoder().withoutPadding().encodeToString(encoded)
    }

    /**
     * True bila [pin] cocok dengan [hashBase64] + [saltBase64].
     *
     * Perbandingan memakai waktu konstan untuk menghindari timing attack. Mengembalikan
     * false (bukan melempar) bila hash/salt belum pernah diset.
     */
    fun verify(pin: String, hashBase64: String, saltBase64: String): Boolean {
        if (hashBase64.isBlank() || saltBase64.isBlank()) return false
        val computed = try {
            hash(pin, saltBase64)
        } catch (_: Exception) {
            return false
        }
        return constantTimeEquals(computed, hashBase64)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}
