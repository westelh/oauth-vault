package dev.westelh

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.westelh.vault.api.identity.response.GetIdentityTokenIssuerKeysResponse
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
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

class ApiTest {
    private val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
    private val mockJWKS = mockServerKeyResponse(keyPair.public as RSAPublicKey)

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun testGetUserId() = testApplication {
        environment { config = MapApplicationConfig(
            "vault.addr" to "http://vault.example.com",
            "api.auth.jwt.audience" to "test-audience",
            "api.auth.jwt.issuer" to "test-issuer",
        ) }
        val testHttp = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        application { configureApi(testHttp) }

        externalServices {
            hosts("http://vault.example.com") {
                install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                    json()
                }
                routing {
                    get("/v1/identity/oidc/.well-known/keys") {
                        call.respond(mockJWKS)
                    }
                }
            }
        }

        testHttp.get("/api/user/id") {
            bearerAuth(signedJWT())
        }.let {
            it.status shouldBe HttpStatusCode.OK
            it.bodyAsText() shouldBe "1234567890"
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun signedJWT(): String {
        val algo = Algorithm.RSA256(keyPair.private as RSAPrivateKey)
        return JWT.create()
            .withClaim("google_id", "1234567890")
            .withKeyId(mockKeyId)
            .withAudience("test-audience")
            .withIssuer("test-issuer")
            .sign(algo)
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun mockServerKeyResponse(publicKey: RSAPublicKey) = GetIdentityTokenIssuerKeysResponse(listOf(
    GetIdentityTokenIssuerKeysResponse.JwkKey(
        kty = "RSA",
        use = "sig",
        alg = "RS256",
        keyId = mockKeyId,
        n = Base64.UrlSafe.encode(publicKey.modulus.toByteArray()),
        e = "AQAB"
    )
))