package dev.westelh

import dev.westelh.service.ApplicationGoogleService
import dev.westelh.service.IdentityService
import dev.westelh.service.KvService
import dev.westelh.vault.Config
import dev.westelh.vault.Vault
import dev.westelh.vault.identity
import dev.westelh.vault.kv
import io.ktor.client.HttpClient
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.html.A
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}

fun Application.ui() {
    configureUserPage()
}

fun Application.api() {
    configureApi()
    configureGoogle()
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

fun Application.createGoogleService(client: HttpClient): ApplicationGoogleService {
    return ApplicationGoogleService(environment.config, client)
}

fun Application.createJwkProvider(client: HttpClient): JwkProvider {
    return JwkProvider(createVaultEngine(client).identity())
}