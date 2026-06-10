package com.fullstackcert.todo.domain.usecase

import com.fullstackcert.todo.domain.model.AuthResult
import com.fullstackcert.todo.domain.model.User
import com.fullstackcert.todo.domain.repository.AuthRepository
import com.fullstackcert.todo.utils.Resource
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Resource<User> =
        repo.register(username, password)
}

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(username: String, password: String, rememberMe: Boolean = false): Resource<AuthResult> =
        repo.login(username, password, rememberMe)
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(): Resource<Unit> = repo.logout()
}

class CheckSessionUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(): Boolean = repo.isSessionValid()
}

class GetSavedCredentialsUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(): Pair<String, Boolean> = repo.getSavedCredentials()
}

class SocialLoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend fun loginWithGoogle(idToken: String): Resource<AuthResult> = repo.loginWithGoogle(idToken)
    suspend fun loginWithFacebook(accessToken: String): Resource<AuthResult> = repo.loginWithFacebook(accessToken)
}
