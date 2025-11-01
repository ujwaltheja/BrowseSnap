package com.tvbrowser.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
sealed class TVResponse {
    @Serializable
    data class SearchResults(val results: List<SearchResult>) : TVResponse()

    @Serializable
    data class NowPlaying(val media: String) : TVResponse()

    @Serializable
    data class Error(val message: String) : TVResponse()
}
