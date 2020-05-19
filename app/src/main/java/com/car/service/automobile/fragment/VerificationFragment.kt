package com.car.service.automobile.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.car.service.automobile.R
import com.car.service.automobile.databinding.FragmentVerificationBinding
import com.car.service.automobile.login.LoginActivity
import com.car.service.automobile.login.LoginViewModel
import com.car.service.automobile.main.MainActivity
import com.car.service.automobile.main.ui.HomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.*

import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * written by robert arnold
 */

class VerificationFragment : Fragment() {

    lateinit var binding: FragmentVerificationBinding
    lateinit var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    private val args: VerificationFragmentArgs by navArgs()

    var verificationNo: String = ""
    private lateinit var viewModel: LoginViewModel


    var phoneNumber = ""
    var name = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_verification, container,
                false
            )

        mAuth = FirebaseAuth.getInstance()
        viewModel = (activity as LoginActivity).viewModel

        phoneNumber = args.phoneNumber
        name = args.name


        verifyPhoneNumber(phoneNumber)
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 30 downTo 0){
                delay(1000)
                withContext(Dispatchers.Main){
                    binding.seconds.text=i.toString()
                }
            }
        }

        binding.verify.setOnClickListener {
            when {
                binding.code.text.toString().isEmpty() -> {
                    Toast.makeText(activity, "field is Empty", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    val code = binding.code.text.toString()
                    authenticate(code)
                }
            }
        }

        return binding.root
    }


    /**
     * function get called to verify the phone number passed from login fragment
     */

    private fun verifyPhoneNumber(phoneNumber: String) {
        verificationCallBack()
        activity?.let {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                it,
                mCallBack
            )
        }
    }

    /**
     * callback method for listening events if the verification of phone number is successfully
     * fail
     */

    private fun verificationCallBack() {
        mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(activity, p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                p1: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, p1)
                verificationNo = verificationId
            }

        }
    }

    /**
     * function get called when the verification of number is successfully hence
     * the user can sign in
     */

    private fun signIn(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                try {
                    val uid = mAuth.uid
                    CoroutineScope(Dispatchers.IO).launch {
                        if (uid != null) {
                            viewModel.signUp(uid, name, phoneNumber)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "Successfully Login", Toast.LENGTH_LONG).show()
                            val intent = Intent(activity, HomeActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }

                } catch (e: FirebaseException) {
                    Toast.makeText(
                        activity,
                        "Some Internal error occur try again later!!!!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                Toast.makeText(activity, "Fail To Sign In", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * function get called up if the phone number is not verified automatically
     * hence user must enter the code that have been received
     */

    private fun authenticate(code: String) {
        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationNo, code)
        signIn(credential)
    }
}
