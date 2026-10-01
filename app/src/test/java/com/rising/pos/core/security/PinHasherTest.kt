package com.rising.pos.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test untuk [PinHasher]: PIN harus di-hash (bukan disimpan/dibandingkan plaintext),
 * verifikasi benar/salah berfungsi, dan salt unik per penyimpanan.
 */
class PinHasherTest {

    @Test
    fun `hash tidak sama dengan pin asli`() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertNotEquals("Hash tidak boleh sama dengan PIN asli", "1234", hash)
        assertTrue(hash.isNotBlank())
    }

    @Test
    fun `verifikasi pin benar`() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("123456", salt)
        assertTrue(PinHasher.verify("123456", hash, salt))
    }

    @Test
    fun `verifikasi pin salah ditolak`() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertFalse(PinHasher.verify("1235", hash, salt))
        assertFalse(PinHasher.verify("9999", hash, salt))
        assertFalse(PinHasher.verify("", hash, salt))
    }

    @Test
    fun `salt berbeda menghasilkan hash berbeda untuk pin sama`() {
        val salt1 = PinHasher.newSalt()
        val salt2 = PinHasher.newSalt()
        assertNotEquals(salt1, salt2)
        assertNotEquals(PinHasher.hash("1234", salt1), PinHasher.hash("1234", salt2))
    }

    @Test
    fun `hash dan salt kosong selalu gagal verifikasi`() {
        assertFalse(PinHasher.verify("1234", "", ""))
        assertFalse(PinHasher.verify("1234", "abc", ""))
        assertFalse(PinHasher.verify("1234", "", "abc"))
    }

    @Test
    fun `verifikasi deterministik untuk salt yang sama`() {
        val salt = PinHasher.newSalt()
        val hash1 = PinHasher.hash("5678", salt)
        val hash2 = PinHasher.hash("5678", salt)
        assertTrue(PinHasher.verify("5678", hash1, salt))
        assertTrue(PinHasher.verify("5678", hash2, salt))
        assertTrue("Hash harus deterministik", hash1 == hash2)
    }
}
