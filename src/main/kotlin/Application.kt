package dev.westelh

import dev.westelh.vault.Config
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}

class VaultApplicationConfig(config: ApplicationConfig): Config {
    override val address: String = config.property("vault.addr").getString()
    override val token: String = config.propertyOrNull("vault.token")?.getString().orEmpty()
    val mount = config.property("vault.kv").getString()
}

fun Application.buildApplicationService(): Service {
    val identity = ApplicationIdentityService(environment.config)
    val kv = ApplicationKvService(environment.config)
    val google = ApplicationGoogleService(environment.config)
    return Service(identity, kv, google)
}