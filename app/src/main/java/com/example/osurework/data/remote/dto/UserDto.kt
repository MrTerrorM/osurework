package com.example.osurework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val username: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    val statistics: UserStatisticsDto
)

data class UserStatisticsDto(
    val pp: Double,
    @SerializedName("global_rank") val globalRank: Int?,
    val accuracy: Double,
    @SerializedName("play_count") val playCount: Int
)