package com.fullstackcert.todo.data.remote.api

import com.fullstackcert.todo.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface TodoApiService {

    @POST("register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("logout")
    suspend fun logout(): Response<MessageResponseDto>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: SocialLoginRequestDto): Response<AuthResponseDto>

    @POST("auth/facebook")
    suspend fun loginWithFacebook(@Body request: SocialLoginRequestDto): Response<AuthResponseDto>

    @GET("todos")
    suspend fun getTodos(): Response<TodoListResponseDto>

    @GET("todos/{id}")
    suspend fun getTodoById(@Path("id") id: Int): Response<TodoResponseDto>

    @POST("todos")
    suspend fun createTodo(@Body request: CreateTodoRequestDto): Response<TodoResponseDto>

    @PUT("todos/{id}")
    suspend fun updateTodo(@Path("id") id: Int, @Body request: UpdateTodoRequestDto): Response<TodoResponseDto>

    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id: Int): Response<MessageResponseDto>

    @POST("todos/bulk-delete")
    suspend fun bulkDelete(@Body request: BulkDeleteRequestDto): Response<MessageResponseDto>

    @Multipart
    @POST("todos/{todoId}/attachments")
    suspend fun uploadAttachment(
        @Path("todoId") todoId: Int,
        @Part file: MultipartBody.Part
    ): Response<AttachmentResponseDto>

    @DELETE("todos/{todoId}/attachments/{attachmentId}")
    suspend fun deleteAttachment(
        @Path("todoId") todoId: Int,
        @Path("attachmentId") attachmentId: Int
    ): Response<MessageResponseDto>
}
