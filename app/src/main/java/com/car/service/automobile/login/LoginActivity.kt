/**
 * written by robert arnold
 */

package com.car.service.automobile.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.car.service.automobile.R
import com.car.service.automobile.databinding.ActivityLoginBinding
import com.car.service.automobile.main.ui.HomeActivity
import com.car.service.automobile.main.ui.MainActivity
import com.car.service.automobile.repository.ApiRepository
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    lateinit var mAuth:FirebaseAuth
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_login
        )

        val firebaseInstance=ApiRepository()
        val factory=LoginViewModelFactory(application,firebaseInstance)
        viewModel=ViewModelProvider(this,factory).get(LoginViewModel::class.java)
        mAuth=FirebaseAuth.getInstance()

    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null){
            val intent=Intent(this,
                HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
}
