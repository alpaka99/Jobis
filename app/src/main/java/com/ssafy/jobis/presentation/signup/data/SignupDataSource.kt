package com.ssafy.jobis.presentation.signup.data

import android.util.Log
import com.ssafy.jobis.data.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.presentation.login.Jobis
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class SignupDataSource {
    lateinit var auth: FirebaseAuth
    suspend fun saveAccount(username: String, nickname: String, password: String): Result<String>? {
        auth = Firebase.auth
        return try {
            var result: Result<String>? = null
            var firestore = FirebaseFirestore.getInstance()
            var user = User(email = username, password = password, nickname = nickname)
            firestore.collection("users")
                .document(auth.currentUser!!.uid)
                .set(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Jobis.prefs.setString("nickname", nickname)
                        result = Result.Success("success")
                    } else {
                        result = Result.Error(IOException("Error logging in"))
                    }
                }.await()
//                .addOnSuccessListener { documentReference ->
//                    Log.d("test", "db sucess ${documentReference}")
//                    result = Result.Success("success")
//                }
//                .addOnFailureListener { e ->
//                    Log.w("test", "db error")
//                    e.printStackTrace()
//                    result = Result.Error(IOException("Error logging in", e))
//                }.await()
            result
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }
    suspend fun signup(username: String, password: String): Boolean {
        return try {
            // TODO: handle loggedInUser authentication
            var result: Boolean = false
            auth = Firebase.auth
            // 1. 계정생성 -> db에 사용자 문서 생성 요청(이걸 백에서 해라..?)
            auth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener() {task ->
                    if (task.isSuccessful) {
                        Log.d("test", "계정 생성됨!")
                        Log.d("test", "uid:: ${auth.currentUser?.uid}")
                        result = true
                    } else {
                        result = false
                    }
                }
                .await()
            result
        } catch (e: Throwable) {
            Log.d("test", "계정 생성 요청이 안됨")
            false
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}