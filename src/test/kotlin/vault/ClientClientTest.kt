package dev.westelh.vault

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test

class ClientClientTest {
    @Test
    fun `Empty vault address should throw exception`() {
        shouldThrow<Throwable> {
            Vault(object : VaultConfig {
                override val address: String = ""
                override val token: String = "xxx"
            })
        }
    }

    @Test
    fun `Test get()`() {
        val engine = MockEngine { req ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK
            )
        }
        val client = Vault(object : VaultConfig {
            override val address: String = "http://localhost:8200"
            override val token: String = "xxx"
        }, engine)

        val res = runBlocking {
            client.get("http://localhost:8200/v1/sys/health") {
                bearerAuth("xxx")
            }
        }

        res.request.headers.contains("Authorization", "Bearer xxx") shouldBe true
    }

    // TODO: Test post
    // TODO: Test delete

    @Test
    fun `Test read`() {
        val engine = MockEngine { req ->
            respond(
                content = ByteReadChannel("""{"foo":"bar"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json")
            )
        }
        val client = Vault(object : VaultConfig {
            override val address: String = "http://localhost:8200"
            override val token: String = "xxx"
        }, engine)

        val res = runBlocking {
            client.readAndGetResult<TestSchema>("http://localhost:8200/v1/sys/health") {
                bearerAuth("xxx")
            }
        }

        res.exceptionOrNull() shouldBe null

        val data = res.getOrNull()

        data shouldBe TestSchema("bar")
    }

    // TODO: Test write
    // TODO: Test delete

    @Serializable
    data class TestSchema(
        val foo: String
    )
}