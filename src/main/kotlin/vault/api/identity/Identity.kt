package dev.westelh.vault.api.identity

import dev.westelh.vault.Vault
import dev.westelh.vault.api.identity.response.GetIdentityTokenIssuerKeysResponse
import dev.westelh.vault.api.identity.response.GetOidcClientResponse

class Identity(private val vault: Vault) {
    suspend fun getIdentityTokenIssuerKeys(): Result<GetIdentityTokenIssuerKeysResponse> {
        return vault.getOrVaultError("identity/oidc/.well-known/keys")
    }

    // OIDC Provider
    suspend fun getOidcClient(name: String): Result<GetOidcClientResponse> {
        return vault.getOrVaultError("identity/oidc/client/$name")
    }
}
