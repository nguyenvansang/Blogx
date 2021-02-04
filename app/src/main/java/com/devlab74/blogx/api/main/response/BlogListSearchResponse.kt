package com.devlab74.blogx.api.main.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BlogListSearchResponse(

    @Json(name = "status")
    var status: String,

    @Json(name = "status_code")
    var statusCode: Int,

    @Json(name = "status_message")
    var statusMessage: String,

    @Json(name = "results")
    var results: List<BlogSearchResponse>

) {

    override fun toString(): String {
        return "BlogListSearchResponse(status='$status', statusCode=$statusCode, statusMessage='$statusMessage', results=$results)"
    }
}