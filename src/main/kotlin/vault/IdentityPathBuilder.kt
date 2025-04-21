package dev.westelh.vault

class IdentityPathBuilder(origin: String) {

    // OIDC Provider
    private val oidcPath = "$origin/v1/identity/oidc"

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