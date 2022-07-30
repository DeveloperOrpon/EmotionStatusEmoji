package com.example.emotionstatusemoji

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
var newUser : Boolean=false
class LoginActivity : AppCompatActivity() {
    companion object{
        private const val RC_CODE=123
        private const val TAG="LoginActivity"
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth=FirebaseAuth.getInstance()
        val gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient=GoogleSignIn.getClient(this,gso)

        signInBtn.setOnClickListener {
            val signinIntent=googleSignInClient.signInIntent
            startActivityForResult(signinIntent,RC_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode== RC_CODE){
            val task=GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account=task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            }
            catch (e:ApiException){
                Log.i(TAG,"Google Sign In Failed",e)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential=GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task->
                if (task.isSuccessful){
                    val user=auth.currentUser
                    updateUI(user)
                }
                else{
                    Toast.makeText(this@LoginActivity,"Login Failed",Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
                newUser= task.getResult().additionalUserInfo?.isNewUser == true

            }

    }

    private fun updateUI(user: FirebaseUser?) {
        //Navigate to main activity
        if (user==null){
            return
        }
        var intent=Intent(this,MainActivity::class.java)
        intent.putExtra("newUser", newUser)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
}