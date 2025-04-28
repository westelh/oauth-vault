package dev.westelh.service

import dev.westelh.VaultApplicationConfig
import dev.westelh.vault.Vault
import dev.westelh.vault.api.identity.Identity
import dev.westelh.vault.identity
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.HttpMethod
import io.ktor.server.application.log
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.runBlocking

interface IdentityService {
    val identity: Identity

    suspend fun getOidcClientId(name: String): Result<String> {
        return identity.readOidcClient(name).map { it.data.clientId }
    }

    suspend fun getOidcClientSecret(name: String): Result<String> {
        return identity.readOidcClient(name).map { it.data.clientSecret }
    }

    fun buildProviderLookup(): OAuthServerSettings.OAuth2ServerSettings

    fun buildOidcAuthorizationEndpointUrl(vaultAddr: String, providerName: String): String {
        // TODO: make this string reusable
        return "$providerName/ui/vault/identity/oidc/provider/$providerName/authorize"
    }

    fun buildOidcTokenEndpointUrl(vaultAddr: String, providerName: String): String {
        val path = Identity.IdentityPathBuilder().buildOidcTokenEndpointPath(providerName)
        return "$vaultAddr/v1/$path"
    }
}

class ApplicationIdentityService(private val config: ApplicationConfig, engine: HttpClientEngine): IdentityService {
    val vault = Vault(VaultApplicationConfig(config), engine)
    override val identity = vault.identity()
    val vaultAddr: String = config.property("vault.addr").getString()
    val providerName: String = config.property("vault.oauth.provider").getString()
    val clientName: String = config.property("vault.oauth.client").getString()

    override fun buildProviderLookup(): OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "vault",
        requestMethod = HttpMethod.Companion.Post,
        authorizeUrl = buildOidcAuthorizationEndpointUrl(vaultAddr, providerName),
        accessTokenUrl = buildOidcTokenEndpointUrl(vaultAddr, providerName),
        clientId = runBlocking { getOidcClientId(clientName) }.getOrThrow(),
        clientSecret = runBlocking { getOidcClientSecret(clientName) }.getOrThrow(),
        defaultScopes = config.property("scopes").getList(),
        onStateCreated = { call, _ ->
            call.request.queryParameters["error"]?.let {
                val desc = call.request.queryParameters["error_description"].orEmpty()
                call.application.log.error("Error during oauth: $it - $desc")
            }
        }
    )
}