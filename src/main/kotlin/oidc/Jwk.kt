package dev.westelh.oidc

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
data class Jwk(
    val kty: String,
    val use: String? = null,
    @SerialName("key_ops")
    val keyOps: List<String>? = null,
    val alg: String? = null,
    val kid: String? = null,
    val x5u: String? = null,
    val x5c: List<String>? = null,
    val x5t: String? = null,
    @SerialName("x5t#S256")
    val x5tS256: String? = null,

    // RSA specific parameters
    val n: String? = null,
    val e: String? = null,
    val d: String? = null,
    val p: String? = null,
    val q: String? = null,
    val dp: String? = null,
    val dq: String? = null,
    val qi: String? = null,

    // EC specific parameters
    val crv: String? = null,
    val x: String? = null,
    val y: String? = null,

    // Symmetric keys
    val k: String? = null
)