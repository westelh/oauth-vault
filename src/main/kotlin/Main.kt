package dev.westelh

import dev.westelh.vault.Vault
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoot() {
    install(Authentication)

    routing {
        get("/") {
            if (call.request.isFromBrowser()) {
                call.respondRedirect("user/summary")
            } else {
                call.respond("Vault OAuth Client")
            }
        }
    }
}

fun makeLogMessage(ctx: RoutingRequest, e: Throwable): String {
    val path = ctx.path()
    val method = ctx.httpMethod.value
    val msg = e.message
    return "$method $path $msg"
}

suspend fun RoutingCall.respondError(e: Throwable) {
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

private fun RoutingRequest.isFromBrowser(): Boolean {
    val userAgent = this.headers["User-Agent"] ?: return false
    val browserNames = listOf("Mozilla", "Chrome", "Safari", "Firefox", "Edge", "Opera")
    val isBrowser = browserNames.any { userAgent.contains(it, ignoreCase = true) }
    return isBrowser
}