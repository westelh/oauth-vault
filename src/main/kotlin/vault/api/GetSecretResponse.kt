package dev.westelh.vault.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://github.com/hashicorp/vault/blob/main/api/secret.go

@Serializable
data class GetSecretResponse<T>(
    // The request ID that generated this response
    @SerialName("request_id")
    val requestId: String,

    @SerialName("lease_id")
    val leaseId: String,
    @SerialName("lease_duration")
    val leaseDuration: Long,
    val renewable: Boolean,

    // Data is the actual contents of the secret. The format of the data
    // is arbitrary and up to the secret backend.
    val data: T,

    // Warnings contains any warnings related to the operation. These
    // are not issues that caused the command to fail, but that the
    // client should be aware of.
    val warnings: List<String>?,

    // WrapInfo, if non-nil, means that the initial response was wrapped in the
    // cubbyhole of the given token (which has a TTL of the given number of
    // seconds)val mountType: String,
    @SerialName("wrap_info")
    val wrapInfo: String?,

    // Auth, if non-nil, means that there was authentication information
    // attached to this response.
    val auth: String?,

    // MountType, if non-empty, provides some information about what kind
    // of mount this secret came from.
    @SerialName("mount_type")
    val mountType: String,
)