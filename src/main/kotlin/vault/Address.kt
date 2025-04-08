package dev.westelh.vault

@JvmInline
value class Address(val url: String) {
    init {
        require(url.startsWith("http://") || url.startsWith("https://")) {
            "URL must start with http:// or https://"
        }

        require(!url.endsWith("/")) {
            "URL must not end with /"
        }
    }
    fun v1(): V1Path {
        return V1Path.create(this)
    }
}

@JvmInline
value class V1Path private constructor(val url: String) {
    companion object {
        fun create(address: Address): V1Path {
            return V1Path("${address.url}/v1")
        }
    }

    fun mount(mount: String): MountedPath {
        return MountedPath.create(this, mount)
    }
}

@JvmInline
value class MountedPath private constructor(val url: String) {
    companion object {
        fun create(v1Path: V1Path, mount: String): MountedPath {
            return MountedPath("${v1Path.url}/$mount")
        }
    }

    fun complete(subPath: String): String {
        return "$url/$subPath"
    }
}
