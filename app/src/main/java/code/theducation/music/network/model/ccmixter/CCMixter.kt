package com.theducation.musicdownloads.module

import com.google.gson.annotations.SerializedName

data class CCMixter (
	@SerializedName("upload_id") val uploadId : Int,
	@SerializedName("upload_name") val uploadName : String,
	@SerializedName("file_page_url") val filePageUrl : String,
	@SerializedName("artist_page_url") val artistPageUrl : String,
	@SerializedName("license_logo_url") val licenseLogoUrl : String,
	@SerializedName("license_url") val licenseUrl : String,
	@SerializedName("license_name") val licenseName : String,
	@SerializedName("user_name") val userName : String,
	@SerializedName("upload_date_format") val uploadDateFormat : String,
	@SerializedName("files") val files : List<Files>,
	@SerializedName("upload_description_plain") val uploadDescriptionPlain : String,
	@SerializedName("upload_description_html") val uploadDescriptionHtml : String,
	@SerializedName("upload_extra") val uploadExtra : UploadExtra
){
	var isPlaying:Boolean = false
}