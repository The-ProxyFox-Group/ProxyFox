package io.github.proxyfox.types

class PkSystem {
    var name: String? = null
    var description: String? = null
    var tag: String? = null
    var avatar_url: String? = null
    var members: Array<PkMember>? = arrayOf()
}


class PkMember {
    var name: String = ""
    var display_name: String? = null
    var description: String? = null
    var pronouns: String? = null
    var color: String? = null
    var keep_proxy: Boolean? = false
    var message_count: Long? = 0
    var proxies: Array<PkProxy>? = arrayOf()
}

class PkProxy {
    var prefix: String? = null
    var suffix: String? = null
}