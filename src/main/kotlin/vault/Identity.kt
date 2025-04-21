package dev.westelh.vault

import dev.westelh.vault.api.identity.GetIdentityTokenIssuerKeysResponse

suspend fun Vault.getIdentityTokenIssuerKeys(): Result<GetIdentityTokenIssuerKeysResponse> {
    return this.readAndGetResult(baseUrl.mount("identity").complete("oidc/.well-known/keys"))
}
