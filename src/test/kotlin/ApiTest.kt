package dev.westelh

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.westelh.vault.api.identity.response.GetIdentityTokenIssuerKeysResponse
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test

private const val mockKeyId = "mock123"
private const val fakeVaultServerHost = "http://vault.example.com"

class ApiTest {
    private val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
    private val mockJWKS = mockServerKeyResponse(keyPair.public as RSAPublicKey)
    private val minimumConfig = mutableListOf<Pair<String, String>>(
        "vault.addr" to fakeVaultServerHost,
        "api.auth.jwt.audience" to "test-audience",
        "api.auth.jwt.issuer" to "test-issuer",
    )

    @Test
    fun testGetUserId() = testApplication {
        val testHttp = createTestClient(client)
        environment {
            config = MapApplicationConfig(minimumConfig)
        }
        application {
            configureApi(testHttp)
        }
        externalServices {
            mockVaultServer(fakeVaultServerHost)
        }

        val res = testHttp.get("/api/user/id") { bearerAuth(signedJWT()) }
        res.status.shouldBe(HttpStatusCode.OK)
        res.bodyAsText().shouldBe("1234567890")
    }

    private fun signedJWT(): String {
        val algo = Algorithm.RSA256(keyPair.private as RSAPrivateKey)
        return JWT.create()
            .withClaim("google_id", "1234567890")
            .withKeyId(mockKeyId)
            .withAudience("test-audience")
            .withIssuer("test-issuer")
            .sign(algo)
    }

    private fun ExternalServicesBuilder.mockVaultServer(host: String, configuration: Routing.() -> Unit = {}) {
        hosts(host) {
            install(ContentNegotiation) {
                json()
            }
            routing {
                get("/v1/identity/oidc/.well-known/keys") {
                    call.respond(mockJWKS)
                }
                configuration()
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun mockServerKeyResponse(publicKey: RSAPublicKey) = GetIdentityTokenIssuerKeysResponse(
    listOf(
        GetIdentityTokenIssuerKeysResponse.JwkKey(
            kty = "RSA",
            use = "sig",
            alg = "RS256",
            keyId = mockKeyId,
            n = Base64.UrlSafe.encode(publicKey.modulus.toByteArray()),
            e = "AQAB"
        )
    )
)

