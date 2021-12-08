package com.ssafy.jobis.presentation.login.data

import android.util.Log
import com.ssafy.jobis.presentation.login.data.model.LoggedInUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Result<LoggedInUser>? {
        return try {
            var user: LoggedInUser
            var result: Result<LoggedInUser>? = null
            var auth = Firebase.auth
            auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        Log.d("test", "lsdfd ${Firebase.auth.currentUser?.uid}")
                        result = Result.Success(LoggedInUser(auth.currentUser?.uid.toString(), auth.currentUser?.email.toString()))
                    } else {
                        result = Result.Error(IOException("Error logging in"))
                    }
                }
                .addOnFailureListener { e ->
                    result = Result.Error(IOException("Error logging in", e))
                }.await()
            // TODO: handle loggedInUser authentication
            return result
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }

    suspend fun getUserInfo(uid: String): String? {
        var nickName: String? = null
        return try {
            var db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { result ->
                    nickName = result.data?.get("nickname") as String?
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }
                .await()
            nickName

        } catch(e: Throwable) {
            e.printStackTrace()
            nickName
        }
    }
}