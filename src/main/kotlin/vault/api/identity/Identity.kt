package dev.westelh.vault.api.identity

import dev.westelh.vault.Vault
import dev.westelh.vault.api.identity.request.*
import dev.westelh.vault.api.identity.response.*
import io.ktor.client.request.*

class Identity(private val vault: Vault) {
    class IdentityPathBuilder {
        // OIDC Provider
        private val oidcPath = "identity/oidc"

        fun buildOidcProviderPath(provider: String): String = "$oidcPath/$provider"
        fun buildOidcProvidersPath(): String = "$oidcPath/provider"

        fun buildOidcScopePath(name: String): String = "$oidcPath/scope/$name"
        fun buildOidcScopesPath(): String = "$oidcPath/scope"

        fun buildOidcClientPath(name: String): String = "$oidcPath/client/$name"
        fun buildOidcClientsPath(): String = "$oidcPath/client"

        fun buildOidcAssignmentPath(name: String): String = "$oidcPath/assignment/$name"
        fun buildOidcAssignmentsPath(): String = "$oidcPath/assignment"

        fun buildOidcConfigurationPath(providerName: String): String
                = "$oidcPath/provider/$providerName/.well-known/openid-configuration"

        fun buildOidcKeysPath(providerName: String): String
                = "$oidcPath/provider/$providerName/.well-known/keys"

        fun buildOidcAuthorizationEndpointPath(providerName: String): String
                = "$oidcPath/provider/$providerName/authorize"

        fun buildOidcTokenEndpointPath(providerName: String): String
                = "$oidcPath/provider/$providerName/token"

        fun buildOidcUserInfoEndpointPath(providerName: String): String
                = "$oidcPath/provider/$providerName/userinfo"
    }

    private val pathBuilder = IdentityPathBuilder()

    // Identity Token
    // https://developer.hashicorp.com/vault/api-docs/secret/identity/tokens
    suspend fun getIdentityTokenIssuerKeys(): Result<GetIdentityTokenIssuerKeysResponse> {
        return vault.getOrVaultError("identity/oidc/.well-known/keys")
    }

    // OIDC Provider
    // https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider
    suspend fun writeOidcProvider(name: String, payload: PutOidcProviderRequest): Result<PutOidcProviderResponse> {
        return vault.postOrVaultError(pathBuilder.buildOidcProviderPath(name)) {
            setBody(payload)
        }
    }

    suspend fun getOidcProvider(name: String): Result<GetOidcProviderResponse> {
        return vault.getOrVaultError(pathBuilder.buildOidcProviderPath(name))
    }

    suspend fun listOidcProviders(): Result<ListOidcProvidersResponse> {
        return vault.listOrVaultError(pathBuilder.buildOidcProvidersPath())
    }

    suspend fun deleteOidcProvider(name: String): Result<DeleteOidcProviderResponse> {
        return vault.deleteOrVaultError(pathBuilder.buildOidcProviderPath(name))
    }

    suspend fun writeOidcScope(name: String, payload: PutOidcScopeRequest): Result<PutOidcScopeResponse> {
        return vault.postOrVaultError(pathBuilder.buildOidcScopePath(name)) {
            setBody(payload)
        }
    }

    suspend fun readOidcScope(name: String): Result<GetOidcScopeResponse> {
        return vault.getOrVaultError(pathBuilder.buildOidcScopePath(name))
    }

    suspend fun listOidcScopes(): Result<ListOidcScopesResponse> {
        return vault.listOrVaultError(pathBuilder.buildOidcScopesPath())
    }

    suspend fun deleteOidcScope(name: String): Result<DeleteOidcScopeResponse> {
        return vault.deleteOrVaultError(pathBuilder.buildOidcScopePath(name))
    }

    suspend fun writeOidcClient(name: String, payload: PutOidcClientRequest): Result<PutOidcClientResponse> {
        return vault.postOrVaultError(pathBuilder.buildOidcClientPath(name)) {
            setBody(payload)
        }
    }

    suspend fun readOidcClient(name: String): Result<GetOidcClientResponse> {
        return vault.getOrVaultError(pathBuilder.buildOidcClientPath(name))
    }

    suspend fun listOidcClients(): Result<ListOidcClientsResponse> {
        return vault.listOrVaultError(pathBuilder.buildOidcClientsPath())
    }

    suspend fun deleteOidcClient(name: String): Result<DeleteOidcClientResponse> {
        return vault.deleteOrVaultError(pathBuilder.buildOidcClientPath(name))
    }

    suspend fun writeOidcAssignment(name: String, payload: PutOidcAssignmentRequest): Result<PutOidcAssignmentResponse> {
        return vault.postOrVaultError(pathBuilder.buildOidcAssignmentPath(name)) {
            setBody(payload)
        }
    }

    suspend fun readOidcAssignment(name: String): Result<GetOidcAssignmentResponse> {
        return vault.getOrVaultError(pathBuilder.buildOidcAssignmentPath(name))
    }

    suspend fun listOidcAssignments(): Result<ListOidcAssignmentsResponse> {
        return vault.listOrVaultError(pathBuilder.buildOidcAssignmentsPath())
    }

    suspend fun deleteOidcAssignment(name: String): Result<DeleteOidcAssignmentResponse> {
        return vault.deleteOrVaultError(pathBuilder.buildOidcAssignmentPath(name))
    }

    suspend fun readOidcProviderConfiguration(providerName: String): Result<GetOidcProviderConfigurationResponse> {
        return vault.getOrVaultError(pathBuilder.buildOidcConfigurationPath(providerName))
    }

    suspend fun readOidcProviderKeys(providerName: String): Result<GetOidcProviderKeysResponse> {
        return vault.getOrVaultError(pathBuilder.buildOidcKeysPath(providerName))
    }
}
