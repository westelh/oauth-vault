package dev.westelh.vault

import dev.westelh.vault.api.identity.IdentityGetJwkResponseData

suspend fun Vault.readOidcWellKnownKeys(): Result<IdentityGetJwkResponseData> {
    return this.readAndGetResult(baseUrl.mount("identity").complete("oidc/.well-known/keys"))
}
