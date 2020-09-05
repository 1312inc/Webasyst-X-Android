package com.webasyst.x.site.domainlist

data class Domain(
    val id: Int,
    val name: String
) {
    constructor(domain: com.webasyst.api.site.Domains.Domain) : this(domain.id, domain.name)
}
