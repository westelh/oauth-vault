package dev.westelh

import dev.westelh.vault.Vault
import dev.westelh.vault.VaultConfig
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}

fun Application.vaultConfig(): VaultConfig = object : VaultConfig {
    override val address: String
        get() = environment.config.property("vault.addr").getString()

    override val token: String
        get() = environment.config.property("vault.token").getString()
}

// TODO: Rename this to "createClient"
fun Application.createVaultClient(): Client {
    val client = Vault(vaultConfig())
    val mount = environment.config.property("vault.kv").getString()
    return Client(client, mount)
}