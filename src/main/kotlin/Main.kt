package dev.westelh

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoot() {
    install(CallLogging)
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

private fun RoutingRequest.isFromBrowser(): Boolean {
    val userAgent = this.headers["User-Agent"] ?: return false
    val browserNames = listOf("Mozilla", "Chrome", "Safari", "Firefox", "Edge", "Opera")
    val isBrowser = browserNames.any { userAgent.contains(it, ignoreCase = true) }
    return isBrowser
}