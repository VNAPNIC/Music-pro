package com.theducation.musicdownloads.module

import com.google.gson.annotations.SerializedName

data class Files(
    @SerializedName("file_id") val fileId: Int,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_num_download") val fileNumDownload: Int,
    @SerializedName("download_url") val downloadUrl: String,
    // /var/www/ccmixter/content/afieled/afieled_-_The_Ballad_of_Robert_Johnson.mp3
    @SerializedName("local_path") val localPath: String,
    @SerializedName("file_format_info") val fileFormatInfo: FileFormatInfo
)

data class FileFormatInfo(
    @SerializedName("mime_type") val mimeType: String,
    @SerializedName("media-type") val mediaType: String,
    @SerializedName("default-ext") val defaultExt: String,
    @SerializedName("ps") val ps: String
)