package io.github.proxyfox.webhook

import dev.kord.common.entity.DiscordAttachment
import dev.kord.core.entity.Webhook

data class ProxyContext(
    var content: String,
    var attachments: List<DiscordAttachment>,
    var webhook: Webhook
)