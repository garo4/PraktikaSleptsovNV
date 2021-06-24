package com.example.praktikasleptsovnv.accounthelper

import android.util.Log
import android.widget.Toast
import com.example.praktikasleptsovnv.MainActivity
import com.example.praktikasleptsovnv.R
import com.example.praktikasleptsovnv.constants.FirebaseAuthConstants
import com.example.praktikasleptsovnv.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*

class AccountHelper (act:MainActivity){
    private val act = act
    private lateinit var signInClient: GoogleSignInClient
    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification(task.result?.user!!)
                    act.uiUpdate(task.result?.user!!)
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                            linkEmailToG(email, password)
                        }
                    }
                    else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                            Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                            Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    fun signInWithEmail(email: String, password: String){
        if(email.isNotEmpty()&& password.isNotEmpty()){
            act.mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {task ->
                if (task.isSuccessful){
                    act.uiUpdate(task.result?.user!!)
                }
                else
                {
                    if(task.exception is FirebaseAuthInvalidCredentialsException){
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD){
                            Toast.makeText(act, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    private fun sendEmailVerification(user: FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(act,act.resources.getString(R.string.send_verification_done),Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(act,act.resources.getString(R.string.send_verification_error),Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getSignInClient():GoogleSignInClient{
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
        requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun signInWithGoogle(){
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        act.startActivityForResult(intent, GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE)
    }
    fun signInFirebaseWithGoogle(token: String){
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(act, "Sign in done", Toast.LENGTH_LONG).show()
                act.uiUpdate(task.result?.user)
            } else{
                Log.d("LogISIP", "Google Sign in exception: ${task.exception}")
            }

        }
    }

    fun linkEmailToG(email: String, password: String) {
        val credential: AuthCredential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, act.resources.getString(R.string.link_done), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(act, act.resources.getString(R.string.enter_to_G), Toast.LENGTH_LONG).show()
        }

    }

    fun signOutGoogle(){
        getSignInClient().signOut()
    }
}