package dev.westelh

import com.auth0.jwk.SigningKeyNotFoundException
import dev.westelh.vault.Vault
import dev.westelh.vault.VaultConfig
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import kotlin.test.Test

class ClientJwkProviderTest {
    private val vaultConfig: VaultConfig = object : VaultConfig {
        override val address: String = "https://example.com:8200"
        override val token: String = ""
    }

    private fun createVaultJwkProviderWithTestConfig(engine: MockEngine): JwkProvider {
        return JwkProvider(Vault(vaultConfig, engine))
    }

    @Test
    fun shouldReturnCorrectKey() {
        // keys.json は以下のような内容で、"test-key-1" を含む:
        // [
        //   { "kid": "test-key-1", "alg": "RS256", ... }
        // ]
        val engine = createMockEngineFromResource("/keys.json")
        val provider = createVaultJwkProviderWithTestConfig(engine)

        val jwk = provider.get("test-key-1")
        jwk.id shouldBe "test-key-1"
    }

    @Test
    fun shouldThrowGivenNonExistentKey() {
        // keys.jsonは"nonexistent-keys"を含まない。
        val engine = createMockEngineFromResource("/keys.json")
        shouldThrowExactly<SigningKeyNotFoundException> {
            createVaultJwkProviderWithTestConfig(engine).get("nonexistentKey")
        }
    }

    @Test
    fun shouldThrowWhenUnauthorized() {
        val engine = createMockEngineNotAuthorized()
        val provider = createVaultJwkProviderWithTestConfig(engine)
        shouldThrowExactly<SigningKeyNotFoundException> {
            provider.get("nonexistentKey")
        }
    }
}