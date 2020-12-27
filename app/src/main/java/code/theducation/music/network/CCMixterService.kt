package code.theducation.music.network

import com.theducation.musicdownloads.module.CCMixter
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface CCMixterService {
    @GET("search")
    suspend fun searchMusicAsync(
        @Query("query") query: String,
        @Query("f") f: String,
        @Query("type") type: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int

    ): ArrayList<CCMixter>
}