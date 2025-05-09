package dev.westelh

import dev.westelh.service.GoogleService
import dev.westelh.service.IdentityService
import dev.westelh.service.KvService
import dev.westelh.vault.Config
import dev.westelh.vault.Vault
import dev.westelh.vault.identity
import dev.westelh.vault.kv
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.tryInstallAuthentication() {
    try {
        install(Authentication)
    } catch (dup: DuplicatePluginException) {
        // Ignore duplicate plugin exception
    }
}

fun Application.root() {
    configureRoot()
}

fun Application.api(httpClient: HttpClient = applicationHttpClient) {
    configureApi(httpClient)
}

fun Application.user(httpClient: HttpClient = applicationHttpClient) {
    configureUserPage(httpClient)
}

fun Application.oidc(httpClient: HttpClient = applicationHttpClient) {
    configureVaultOIDC(httpClient)
}

fun Application.oauth(httpClient: HttpClient = applicationHttpClient) {
    configureGoogleOAuth(httpClient)
}

@OptIn(ExperimentalSerializationApi::class)
val applicationHttpClient: HttpClient by lazy {
    HttpClient(Apache) {
        install(ContentNegotiation) {
            json(Json {
                allowTrailingComma = true
            })
        }
    }
}

class VaultApplicationConfig(config: ApplicationConfig): Config {
    override val address: String = config.property("vault.addr").getString()
    override val token: String = config.propertyOrNull("vault.token")?.getString().orEmpty()
}

private fun Application.createVaultEngine(client: HttpClient): Vault {
    val config = VaultApplicationConfig(environment.config)
    return Vault(config, client)
}

fun Application.createKvService(client: HttpClient): KvService {
    val mount = environment.config.property("vault.kv").getString()
    return KvService(createVaultEngine(client).kv(mount))
}

fun Application.createIdService(client: HttpClient): IdentityService {
    return IdentityService(createVaultEngine(client).identity())
}

fun Application.createGoogleService(client: HttpClient): GoogleService {
    return GoogleService(client, ApplicationGoogleConfig(environment.config))
}

