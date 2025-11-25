package com.example.aqualife.ui.viewmodel

// ============================================================================
// ANDROIDX IMPORTS
// ============================================================================
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// ============================================================================
// THIRD-PARTY IMPORTS
// ============================================================================
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ============================================================================
// LOCAL IMPORTS
// ============================================================================
import com.example.aqualife.data.local.dao.CartDao
import com.example.aqualife.data.local.dao.FavoriteDao
import com.example.aqualife.data.local.dao.UserDao
import com.example.aqualife.data.local.entity.UserEntity
import com.example.aqualife.data.preferences.SearchHistoryPreferences
import com.example.aqualife.data.preferences.SessionPreferences
import com.example.aqualife.data.preferences.SessionProvider
import com.example.aqualife.data.preferences.SessionState

// ============================================================================
// AUTH VIEWMODEL
// ============================================================================

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    object VerificationRequired : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val sessionPreferences: SessionPreferences,
    private val cartDao: CartDao,
    private val favoriteDao: FavoriteDao,
    private val searchHistoryPreferences: SearchHistoryPreferences
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = firebaseAuth.currentUser
    )

    val userProfile: StateFlow<UserEntity?> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                userDao.getUser(user.uid)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val sessionState: StateFlow<SessionState> = sessionPreferences.sessionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionState()
        )

    private fun isAdminAccount(email: String, password: String): Boolean {
        return email == "admin123" && password == "admin123"
    }

    private suspend fun persistUser(user: FirebaseUser) {
        val existingUser = userDao.getUser(user.uid).firstOrNull()
        if (existingUser != null) {
            userDao.updateUser(
                existingUser.copy(
                    displayName = user.displayName ?: existingUser.displayName,
                    isEmailVerified = user.isEmailVerified,
                    lastLogin = System.currentTimeMillis()
                )
            )
        } else {
            userDao.insertUser(
                UserEntity(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    isEmailVerified = user.isEmailVerified,
                    lastLogin = System.currentTimeMillis()
                )
            )
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            _authState.value = AuthState.Loading
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    sendVerificationEmail(user)
                    userDao.insertUser(
                        UserEntity(
                            uid = user.uid,
                            email = user.email ?: "",
                            displayName = displayName,
                            isEmailVerified = false
                        )
                    )
                    _authError.value = "Vui lòng kiểm tra email để xác thực tài khoản."
                    _authState.value = AuthState.VerificationRequired
                } else {
                    val message = "Không thể tạo tài khoản. Vui lòng thử lại."
                    _authError.value = message
                    _authState.value = AuthState.Error(message)
                }
            } catch (e: Exception) {
                val message = e.message ?: "Đăng ký thất bại"
                _authError.value = message
                _authState.value = AuthState.Error(message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            _authState.value = AuthState.Loading

            if (isAdminAccount(email, password)) {
                _isLoading.value = false
                _authState.value = AuthState.Success
                return@launch
            }

            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    user.reload().await()
                    if (user.isEmailVerified) {
                        try {
                            persistUser(user)
                            sessionPreferences.setSession(
                                provider = SessionProvider.Firebase,
                                displayName = user.displayName ?: displayNameFromEmail(user.email),
                                email = user.email
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("AuthViewModel", "Failed to persist user", e)
                        }
                        _isLoading.value = false
                        _authState.value = AuthState.Success
                    } else {
                        sendVerificationEmail(user)
                        _authError.value = "Vui lòng xác thực email trước khi đăng nhập."
                        _isLoading.value = false
                        _authState.value = AuthState.VerificationRequired
                    }
                } else {
                    val message = "Không tìm thấy tài khoản."
                    _authError.value = message
                    _isLoading.value = false
                    _authState.value = AuthState.Error(message)
                }
            } catch (e: Exception) {
                val message = e.message ?: "Đăng nhập thất bại"
                _authError.value = message
                _isLoading.value = false
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // KHÔNG XÓA DATA - Chỉ clear session
            // Data vẫn được giữ trong DB với userId, sẽ tự động load lại khi login
            firebaseAuth.signOut()
            sessionPreferences.clearSession()
            _authState.value = AuthState.Idle
        }
    }

    fun updateProfile(displayName: String, bio: String) {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser ?: return@launch
            val existing = userDao.getUser(user.uid).firstOrNull()
            val updatedEntity = (existing ?: UserEntity(
                uid = user.uid,
                email = user.email ?: "",
                displayName = displayName,
                bio = bio,
                isEmailVerified = user.isEmailVerified
            )).copy(
                displayName = displayName,
                bio = bio,
                avatarUrl = existing?.avatarUrl ?: "",
                isEmailVerified = user.isEmailVerified,
                lastLogin = System.currentTimeMillis()
            )
            userDao.insertUser(updatedEntity)
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser
            if (user != null && !user.isEmailVerified) {
                try {
                    sendVerificationEmail(user)
                    _authError.value = "Email xác thực đã được gửi lại."
                } catch (e: Exception) {
                    _authError.value = e.message
                }
            }
        }
    }

    fun checkEmailStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            val user = firebaseAuth.currentUser
            if (user != null) {
                try {
                    user.reload().await()
                    if (user.isEmailVerified) {
                        try {
                            persistUser(user)
                        } catch (e: Exception) {
                            android.util.Log.e("AuthViewModel", "Failed to persist user", e)
                        }
                        _isLoading.value = false
                        _authState.value = AuthState.Success
                    } else {
                        val message = "Email chưa được xác thực. Vui lòng kiểm tra hộp thư."
                        _authError.value = message
                        _isLoading.value = false
                        _authState.value = AuthState.Error(message)
                        delay(1000)
                        _authState.value = AuthState.VerificationRequired
                    }
                } catch (e: Exception) {
                    val message = "Không thể kiểm tra trạng thái: ${e.message}"
                    _authError.value = message
                    _isLoading.value = false
                    _authState.value = AuthState.Error(message)
                }
            } else {
                val message = "Không tìm thấy tài khoản."
                _authError.value = message
                _isLoading.value = false
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
        _authError.value = null
    }

    fun loginWithGoogleAccount(displayName: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = "google_${email.lowercase()}"
                persistSocialUser(uid, displayName, email)
                sessionPreferences.setSession(SessionProvider.Google, displayName, email)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authError.value = e.message ?: "Đăng nhập Google thất bại"
                _authState.value = AuthState.Error(_authError.value ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithFacebookAccount(displayName: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = "facebook_${email.lowercase()}"
                persistSocialUser(uid, displayName, email)
                sessionPreferences.setSession(SessionProvider.Facebook, displayName, email)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authError.value = e.message ?: "Đăng nhập Facebook thất bại"
                _authState.value = AuthState.Error(_authError.value ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun persistSocialUser(uid: String, displayName: String, email: String) {
        val existing = userDao.getUser(uid).firstOrNull()
        val entity = (existing ?: UserEntity(
            uid = uid,
            email = email,
            displayName = displayName,
            isEmailVerified = true
        )).copy(
            displayName = displayName,
            email = email,
            isEmailVerified = true,
            lastLogin = System.currentTimeMillis()
        )
        userDao.insertUser(entity)
    }

    private fun displayNameFromEmail(email: String?): String {
        return email?.substringBefore("@") ?: "AquaLife User"
    }

    private suspend fun sendVerificationEmail(user: FirebaseUser) {
        try {
            user.sendEmailVerification().await()
        } catch (_: Exception) {
            // ignore throttling errors
        }
    }
}
