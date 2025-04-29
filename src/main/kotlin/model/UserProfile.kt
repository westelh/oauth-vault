package dev.westelh.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    @SerialName("given_name")
    val givenName: String,
    @SerialName("family_name")
    val familyName: String,
    val picture: String,
    val email: String,
    @SerialName("verified_email")
    val verifiedEmail: Boolean
)