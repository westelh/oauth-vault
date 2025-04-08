package dev.westelh

import dev.westelh.vault.Vault
import dev.westelh.vault.VaultConfig
import dev.westelh.vault.api.VaultErrorResponse
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ClientTest {
    private val vaultConfig: VaultConfig = object : VaultConfig {
        override val address: String = "https://example.com:8200"
        override val token: String = ""
    }

    private fun createTestClient(engine: MockEngine): Client {
        val vault = Vault(vaultConfig, engine)
        return Client(vault, "kv")
    }

    @Test
    fun shouldReadTokenSuccessfully(): Unit = runBlocking {
        val engine = createMockEngineFromResource("/kv.json")
        val client = createTestClient(engine)

        val result = client.readToken("test")

        result.isSuccess shouldBe true
        result.getOrNull().shouldNotBeNull {
            // this <- OAuthCodes
            refreshToken.shouldBe("test-refresh-token")
        }
    }

    @Test
    fun testReadTokenEmptyError(): Unit = runBlocking {
        val engine = createMockEngineRespondsWithErrors(VaultErrorResponse(emptyList()), HttpStatusCode.OK)
        val client = createTestClient(engine)

        val result = client.readToken("test")

        result.isSuccess shouldBe false
    }
}