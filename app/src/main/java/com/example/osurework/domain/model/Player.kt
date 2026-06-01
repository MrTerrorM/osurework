package com.example.osurework.domain.model

data class Player(
    val id: Int,
    val username: String,
    val avatarUrl: String,
    val globalRank: Int?,
    val pp: Double,
    val accuracy: Double,
    val playCount: Int
)