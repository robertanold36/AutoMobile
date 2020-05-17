package com.car.service.automobile.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.car.service.automobile.R
import com.car.service.automobile.databinding.FragmentLoginBinding
import com.car.service.automobile.login.LoginActivity
import com.car.service.automobile.login.LoginViewModel

/**
 * A simple [Fragment] subclass.
 * written by robert arnold
 */


class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: LoginViewModel
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

        binding.signUp.setOnClickListener {
            when {
                binding.phoneNumber.text.toString().isEmpty() -> {
                    Toast.makeText(activity, "phone number input is Empty", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.name.text.toString().isEmpty() -> {
                    Toast.makeText(activity, "name input is Empty", Toast.LENGTH_SHORT).show()

                }
                binding.phoneNumber.text.toString().length < 9 -> {
                    Toast.makeText(activity, "phone number is less as required", Toast.LENGTH_SHORT)
                        .show()

                }
                binding.phoneNumber.text.toString().length > 9 -> {
                    Toast.makeText(activity, "phone number length is greater", Toast.LENGTH_SHORT)
                        .show()

                }
                else -> {
                    val bundle = Bundle()
                    bundle.putString("phoneNumber", "+255" + binding.phoneNumber.text.toString())
                    bundle.putString("name", binding.name.text.toString())
                    findNavController().navigate(
                        R.id.action_loginFragment_to_verificationFragment,
                        bundle
                    )
                }
            }
        }

        return binding.root
    }

}
