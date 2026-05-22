package com.fullstackcert.todo.di

import com.fullstackcert.todo.BuildConfig
import com.fullstackcert.todo.data.local.SessionManager
import com.fullstackcert.todo.data.remote.api.TodoApiService
import com.fullstackcert.todo.data.repository.AuthRepositoryImpl
import com.fullstackcert.todo.data.repository.TodoRepositoryImpl
import com.fullstackcert.todo.domain.repository.AuthRepository
import com.fullstackcert.todo.domain.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { sessionManager.token.first() }
            val request = chain.request().newBuilder().apply {
                addHeader("Accept", "application/json")
                if (!token.isNullOrEmpty()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): TodoApiService =
        retrofit.create(TodoApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthRepository(api: TodoApiService, sessionManager: SessionManager): AuthRepository =
        AuthRepositoryImpl(api, sessionManager)

    @Provides
    @Singleton
    fun provideTodoRepository(api: TodoApiService): TodoRepository =
        TodoRepositoryImpl(api)
}
