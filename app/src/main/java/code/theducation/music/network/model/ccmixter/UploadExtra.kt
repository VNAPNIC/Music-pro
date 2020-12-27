package com.theducation.musicdownloads.module

import com.google.gson.annotations.SerializedName


data class UploadExtra(
    @SerializedName("usertags") val usertags: String,
    @SerializedName("bpm-type") val bpm: Int
)