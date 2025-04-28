package dev.westelh

import dev.westelh.vault.Vault
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond("Vault OAuth Client")
        }
    }
}



suspend fun RoutingCall.respond(e: Throwable) {
    val path = this.request.path()

    when (e) {
        is Vault.VaultError -> {
            this.respond(e.response.status, "$path ${e.message}")
        }

        else -> {
            this.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
        }
    }
}