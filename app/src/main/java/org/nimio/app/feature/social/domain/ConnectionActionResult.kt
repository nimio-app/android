package org.nimio.app.feature.social.domain

data class ConnectionActionResult(
    val connection: ConnectionSummary,
    val message: String? = null
)

