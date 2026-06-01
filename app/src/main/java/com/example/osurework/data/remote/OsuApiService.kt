package com.example.osurework.data.remote

import com.example.osurework.data.remote.dto.BeatmapAttributesRequest
import com.example.osurework.data.remote.dto.BeatmapAttributesResponse
import com.example.osurework.data.remote.dto.BeatmapDto
import com.example.osurework.data.remote.dto.ScoreDto
import com.example.osurework.data.remote.dto.UserDto
import retrofit2.http.*

interface OsuApiService {

    @GET("users/{username}/osu")
    suspend fun getUser(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Query("key") key: String = "username"
    ): UserDto

    @GET("users/{userId}/scores/best")
    suspend fun getTopScores(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Query("limit") limit: Int = 100,
        @Query("mode") mode: String = "osu"
    ): List<ScoreDto>

    @GET("beatmaps/{beatmapId}")
    suspend fun getBeatmap(
        @Header("Authorization") token: String,
        @Path("beatmapId") beatmapId: Int
    ): BeatmapDto

    @POST("beatmaps/{beatmapId}/attributes")
    suspend fun getBeatmapAttributes(
        @Header("Authorization") token: String,
        @Path("beatmapId") beatmapId: Int,
        @Body body: BeatmapAttributesRequest
    ): BeatmapAttributesResponse
}