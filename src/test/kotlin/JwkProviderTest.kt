package dev.westelh

import com.auth0.jwk.SigningKeyNotFoundException
import dev.westelh.vault.Vault
import dev.westelh.vault.api.identity.Identity
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class JwkProviderTest {
    @Test
    fun shouldReturnCorrectKey() = testApplication {
        // keys.json は以下のような内容で、"test-key-1"を含む:
        // [
        //   { "kid": "test-key-1", "alg": "RS256", ... }
        // ]
        val engine = createMockEngineFromResource("/keys.json")

        environment {
            config = ApplicationConfig("application.yaml")
        }

        application {
            val provider = createJwkProvider(engine)


            val jwk = provider.get("test-key-1")
            jwk.id shouldBe "test-key-1"
        }
    }

    @Test
    fun shouldThrowGivenNonExistentKey() = testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
        }

        // keys.jsonは"nonexistent-keys"を含まない。
        val engine = createMockEngineFromResource("/keys.json")

        application {
            val provider = createJwkProvider(engine)

            shouldThrowExactly<SigningKeyNotFoundException> {
                provider.get("nonexistent-key")
            }
        }
    }

    @Test
    fun shouldThrowWhenUnauthorized() = testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
        }
        application {
            val engine = createMockEngineNotAuthorized()
            val provider = createJwkProvider(engine)
            shouldThrowExactly<SigningKeyNotFoundException> {
                provider.get("nonexistentKey")
            }
        }
    }
}