package code.theducation.music.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.*

interface SuggestionService {
    @Headers(
        value = [
            "Accept: application/vnd.github.v3.full+json",
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36"
        ]
    )
    @GET("search")
    suspend fun searchSuggestAsync(
        @Query("q") value: String,
        @Query("client") client: String,
        @Query("hl") h1: String
    ): ArrayList<Any>
}