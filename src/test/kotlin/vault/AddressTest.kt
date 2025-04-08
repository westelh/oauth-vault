package dev.westelh.vault

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AddressTest {
    @Test
    fun `Address should be created with http`() {
        Address("http://localhost")
    }

    @Test
    fun `Address should be created with https`() {
        Address("https://localhost")
    }

    @Test
    fun `Address should not be created with ftp`() {
        shouldThrow<IllegalArgumentException> {
            Address("ftp://localhost")
        }
    }

    @Test
    fun `Address should not be created with empty string`() {
        shouldThrow<IllegalArgumentException> {
            Address("")
        }
    }

    @Test
    fun `V1 path is ok`() {
        val address = Address("http://localhost").v1()
        address.url shouldBe "http://localhost/v1"
    }

    @Test
    fun `Mounted path is ok`() {
        val address = Address("http://localhost").v1().mount("secrets")
        address.url shouldBe "http://localhost/v1/secrets"
    }

    @Test
    fun `Subpath is ok`() {
        val address = Address("http://localhost").v1().mount("secrets").complete("my-secret")
        address shouldBe "http://localhost/v1/secrets/my-secret"
    }
}