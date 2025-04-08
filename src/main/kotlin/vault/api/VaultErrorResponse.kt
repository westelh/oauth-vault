package dev.westelh.vault.api

import kotlinx.serialization.Serializable

@Serializable
data class VaultErrorResponse(val errors: List<String>) {
    override fun toString(): String {
        return errors.joinToString(separator = ",")
    }
}