package code.theducation.music.network

import android.content.Context
import code.theducation.music.App
import code.theducation.music.BuildConfig
import code.theducation.music.network.conversion.LyricsConverterFactory
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


fun provideDefaultCache(): Cache? {
    val cacheDir = File(App.getContext().cacheDir.absolutePath, "/okhttp-lastfm/")
    if (cacheDir.mkdirs() || cacheDir.isDirectory) {
        return Cache(cacheDir, 1024 * 1024 * 10)
    }
    return null
}

fun logInterceptor(): Interceptor {
    val loggingInterceptor = HttpLoggingInterceptor()
    if (BuildConfig.DEBUG) {
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
    } else {
        // disable retrofit log on release
        loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
    }
    return loggingInterceptor
}

fun headerInterceptor(context: Context): Interceptor {
    return Interceptor {
        val original = it.request()
        val request = original.newBuilder()
            .header("User-Agent", context.packageName)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .method(original.method(), original.body())
            .build()
        it.proceed(request)
    }
}

fun provideOkHttp(context: Context, cache: Cache): OkHttpClient {
    // Create a trust manager that does not validate certificate chains
    val trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(
        object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {}

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    // Install the all-trusting trust manager
    val sslContext: SSLContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    // Create an ssl socket factory with our all-trusting manager
    val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()
    builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
    builder.hostnameVerifier(HostnameVerifier { _, _ -> true })

    return builder
        .addNetworkInterceptor(logInterceptor())
        //.addInterceptor(headerInterceptor(context))
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .cache(cache)
        .build()
}

fun provideLastFmRetrofit(client: OkHttpClient): Retrofit {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    return Retrofit.Builder()
        .baseUrl("https://ws.audioscrobbler.com/2.0/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory { request -> client.newCall(request) }
        .build()
}

fun provideLastFmRest(retrofit: Retrofit): LastFMService {
    return retrofit.create(LastFMService::class.java)
}

fun provideSuggestion(retrofit: Retrofit): SuggestionService {
    val newBuilder = retrofit.newBuilder()
        .baseUrl("http://suggestqueries.google.com/complete/")
        .build()
    return newBuilder.create(SuggestionService::class.java)
}

fun provideCCMixter(retrofit: Retrofit): CCMixterService {
    val newBuilder = retrofit.newBuilder()
        .baseUrl("http://ccmixter.org/api/pool/")
        .build()
    return newBuilder.create(CCMixterService::class.java)
}

fun provideDeezerRest(retrofit: Retrofit): DeezerService {
    val newBuilder = retrofit.newBuilder()
        .baseUrl("https://api.deezer.com/")
        .build()
    return newBuilder.create(DeezerService::class.java)
}

fun provideLyrics(retrofit: Retrofit): LyricsRestService {
    val newBuilder = retrofit.newBuilder()
        .baseUrl("https://makeitpersonal.co")
        .addConverterFactory(LyricsConverterFactory())
        .build()
    return newBuilder.create(LyricsRestService::class.java)
}