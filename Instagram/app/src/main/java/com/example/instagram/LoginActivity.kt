package com.example.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagram.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.emailLoginButton.setOnClickListener {
           //signinAndSignup()
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun signinAndSignup() {
            var email = binding.emailEdittext.toString().trim()
            var password = binding.passwordEdittext.toString().trim()
            auth?.createUserWithEmailAndPassword(email, password)?.
            addOnCompleteListener(this) {
                    task ->
                if(task.isSuccessful){
                    //Creating a user account
                    val user = auth!!.currentUser
                    moveMainPage(user)
                }else if(task.exception?.message.isNullOrEmpty()){
                    //Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
                    //Login if you have account
                    signinEmail(email, password)
                }
            }
    }

    private fun signinEmail(email: String, password: String) {
        auth?.signInWithEmailAndPassword(email, password)?.
        addOnCompleteListener(this) {
                task ->
            if(task.isSuccessful){
                //Creating a user account
                val user = auth!!.currentUser
                moveMainPage(user)
            }else
                //Show the error message
                Toast.makeText(this, task.exception?.message , Toast.LENGTH_LONG).show()
        }
    }
    private fun moveMainPage(user: FirebaseUser?) {
        if(user!= null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}