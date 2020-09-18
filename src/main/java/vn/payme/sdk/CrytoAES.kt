package vn.payme.sdk
import android.util.Base64
import java.math.BigInteger
import java.security.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.US_ASCII
import kotlin.text.Charsets.UTF_8

class CryptoAES {
    private val SALTED_STR = "Salted__"
    private val SALTED_MAGIC: ByteArray = SALTED_STR.toByteArray(US_ASCII)

    fun encryptAES(password: String, clearText: String): String {
        val pass: ByteArray = password.toByteArray(US_ASCII)
        val salt: ByteArray = SecureRandom().generateSeed(8)
        val inBytes: ByteArray = clearText.toByteArray(UTF_8)
        val passAndSalt: ByteArray = arrayConcat(pass, salt)
        var hash = ByteArray(0)
        var keyAndIv = ByteArray(0)
        var i = 0
        while (i < 3 && keyAndIv.size < 48) {
            val hashData: ByteArray = arrayConcat(hash, passAndSalt)
            val md: MessageDigest = MessageDigest.getInstance("MD5")
            hash = md.digest(hashData)
            keyAndIv = arrayConcat(keyAndIv, hash)
            i++
        }
        val keyValue: ByteArray = keyAndIv.copyOfRange(0, 32)
        val iv: ByteArray = keyAndIv.copyOfRange(32, 48)
        val key = SecretKeySpec(keyValue, "AES")
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        var data: ByteArray? = cipher.doFinal(inBytes)
        data = arrayConcat(SALTED_MAGIC, salt).let { data?.let { it1 -> arrayConcat(it, it1) } }
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun decryptAES(password: String, source: String?): String? {
        val pass: ByteArray = password.toByteArray(US_ASCII)
        val inBytes: ByteArray = Base64.decode(source, Base64.DEFAULT)
        val shouldBeMagic = inBytes.copyOfRange(0, SALTED_MAGIC.size)
        require(shouldBeMagic.contentEquals(SALTED_MAGIC)) { "Initial bytes from input do not match OpenSSL SALTED_MAGIC salt value." }
        val salt = inBytes.copyOfRange(SALTED_MAGIC.size, SALTED_MAGIC.size + 8)
        val passAndSalt = arrayConcat(pass, salt)
        var hash = ByteArray(0)
        var keyAndIv = ByteArray(0)
        var i = 0
        while (i < 3 && keyAndIv.size < 48) {
            val hashData = arrayConcat(hash, passAndSalt)
            val md = MessageDigest.getInstance("MD5")
            hash = md.digest(hashData)
            keyAndIv = arrayConcat(keyAndIv, hash)
            i++
        }
        val keyValue = keyAndIv.copyOfRange(0, 32)
        val key = SecretKeySpec(keyValue, "AES")
        val iv = keyAndIv.copyOfRange(32, 48)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val clear = cipher.doFinal(inBytes, 16, inBytes.size - 16)
        return String(clear, UTF_8)
    }

    private fun arrayConcat(a: ByteArray, b: ByteArray): ByteArray {
        val c = ByteArray(a.size + b.size)
        System.arraycopy(a, 0, c, 0, a.size)
        System.arraycopy(b, 0, c, a.size, b.size)
        return c
    }

    @Throws(NoSuchAlgorithmException::class)
    fun getMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(input.toByteArray())
        val no = BigInteger(1, messageDigest)
        var hashtext: String = no.toString(16)
        while (hashtext.length < 32) {
            hashtext = "0$hashtext"
        }
        return hashtext
    }
}
