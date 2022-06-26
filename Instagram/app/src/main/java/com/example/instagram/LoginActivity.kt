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
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
        }
    }

    fun signinAndSignup() {

        val email = binding.emailEdittext.toString()
        val password = binding.passwordEdittext.toString()

        if( email.isBlank() || password.isBlank()){
            Toast.makeText(this, "Email/Password 를 입력하세요.", Toast.LENGTH_LONG).show()
        }else{
            auth?.createUserWithEmailAndPassword(binding.emailEdittext.toString(), binding.passwordEdittext.toString())?.
            addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    //Creating a user account
                    moveMainPage(task.result?.user)
                }else if(task.exception?.message.isNullOrEmpty()){
                    //Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
                    //Login if you have account
                    signinEmail()
                }
            }
        }
    }
    fun signinEmail() {
        auth?.createUserWithEmailAndPassword(binding.emailEdittext.toString(), binding.passwordEdittext.toString())?.
        addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                //Creating a user account
                moveMainPage(task.result?.user)
            }else
                //Show the error message
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
        }
    }
    fun moveMainPage(user: FirebaseUser?) {
        if(user!= null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}