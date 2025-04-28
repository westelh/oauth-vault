package dev.westelh

import dev.westelh.service.ApplicationGoogleService
import dev.westelh.service.IdentityService
import dev.westelh.service.KvService
import dev.westelh.vault.Config
import dev.westelh.vault.Vault
import dev.westelh.vault.identity
import dev.westelh.vault.kv
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.server.application.*
import io.ktor.server.config.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
    configureUserPage()
    configureGoogle()
    configureApi()
}

class VaultApplicationConfig(config: ApplicationConfig): Config {
    override val address: String = config.property("vault.addr").getString()
    override val token: String = config.propertyOrNull("vault.token")?.getString().orEmpty()
}

private fun Application.createVaultEngine(engine: HttpClientEngine = Apache.create {  }): Vault {
    val config = VaultApplicationConfig(environment.config)
    return Vault(config, engine)
}

fun Application.createKvService(engine: HttpClientEngine = Apache.create {  }): KvService {
    val mount = environment.config.property("vault.kv").getString()
    return KvService(createVaultEngine(engine).kv(mount))
}

fun Application.createIdService(engine: HttpClientEngine = Apache.create {  }): IdentityService {
    return IdentityService(createVaultEngine(engine).identity())
}

fun Application.createGoogleService(engine: HttpClientEngine = Apache.create {  }): ApplicationGoogleService {
    return ApplicationGoogleService(environment.config, engine)
}

fun Application.createJwkProvider(engine: HttpClientEngine = Apache.create {  }): JwkProvider {
    return JwkProvider(createVaultEngine(engine).identity())
}