package dev.westelh.vault.api.identity.response

import com.nimbusds.jose.jwk.JWK
import dev.westelh.model.OpenIdProviderMetadata

// Provider Configuration
typealias GetOidcProviderConfigurationResponse = OpenIdProviderMetadata

// Provider Keys
typealias GetOidcProviderKeysResponse = GetOidcProviderKeysResponseData
data class GetOidcProviderKeysResponseData(
    val keys: List<JWK>,
)
