package dev.westelh

import dev.westelh.service.ApplicationGoogleService
import dev.westelh.service.ApplicationIdentityService
import dev.westelh.service.ApplicationKvService
import dev.westelh.service.Service
import dev.westelh.vault.Config
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
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

fun Application.buildApplicationService(engine: HttpClientEngine = Apache.create {  }): Service {
    val identity = ApplicationIdentityService(environment.config, engine)
    val kv = ApplicationKvService(environment.config, engine)
    val google = ApplicationGoogleService(environment.config, engine)
    return Service(identity, kv, google)
}