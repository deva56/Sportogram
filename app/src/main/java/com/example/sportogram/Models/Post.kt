package com.example.sportogram.Models

data class Post(
    val createdBefore: String,
    val author: Author,
    val video: Video,
    val description: String,
    val views: String
) {
}
