package dev.westelh.vault

import dev.westelh.vault.api.identity.response.GetIdentityTokenIssuerKeysResponse

suspend fun Vault.getIdentityTokenIssuerKeys(): Result<GetIdentityTokenIssuerKeysResponse> {
    return getOrVaultError(baseUrl.mount("identity").complete("oidc/.well-known/keys"))
}
