package com.innerken.aadenprinterx.modules.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.innerken.aadenprinterx.dataLayer.api.PrinterService
import com.innerken.aadenprinterx.modules.GlobalSettingManager
import com.innerken.aadenprinterx.modules.LocalDateAdapter
import com.innerken.aadenprinterx.modules.LocalDateTimeAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.innerken.aadenprinterx.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object NetModule {

    @Provides
    fun provideBaseUrl(globalSettingManager: GlobalSettingManager): String {
        return globalSettingManager.getEndPoint()
    }

    @Provides
    fun provideGson() =
        GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .setLenient()
            .create()

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder().writeTimeout(15, TimeUnit.MINUTES)
            .readTimeout(15, TimeUnit.MINUTES)
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String, gson: Gson): Retrofit =
        // <-- Modified line
        Retrofit.Builder().addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))  // <-- Modified line
            .baseUrl(baseUrl).client(okHttpClient).build()


    @Provides
    @Singleton
    fun providePrinterService(retrofit: Retrofit): PrinterService =
        retrofit.create(PrinterService::class.java)


}