package dev.westelh.vault.api

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val errors: List<String>) {
    override fun toString(): String {
        return "Vault Error: ${errors.map { it.replace("\n", "") }}"
    }
}