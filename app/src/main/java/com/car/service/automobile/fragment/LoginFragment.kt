package com.car.service.automobile.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.car.service.automobile.R
import com.car.service.automobile.databinding.FragmentLoginBinding
import com.car.service.automobile.login.LoginActivity
import com.car.service.automobile.login.LoginViewModel
import com.car.service.automobile.loginListener.LoginListener
import com.car.service.automobile.utility.Constants.Companion.phoneCode

/**
 * A simple [Fragment] subclass.
 * written by robert arnold
 */


class LoginFragment : Fragment(), LoginListener {

    lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: LoginViewModel

    private val phoneNumberKey = "phoneNumber"
    private val nameKey = "name"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )

        viewModel = (activity as LoginActivity).viewModel
        binding.loginViewModel=viewModel
        viewModel.listener=this

        return binding.root
    }

    override fun onFail(message: String) {
        binding.errorMsg.text=message
    }

    override fun onSuccess(phoneNumber: String, name: String) {
        binding.errorMsg.text=""
        val bundle = Bundle()
        bundle.putString(phoneNumberKey,phoneCode + phoneNumber)
        bundle.putString(nameKey, name)
        findNavController().navigate(
            R.id.action_loginFragment_to_verificationFragment,
            bundle
        )
    }

}
