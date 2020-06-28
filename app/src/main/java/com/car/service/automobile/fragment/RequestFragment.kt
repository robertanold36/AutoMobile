package com.car.service.automobile.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.car.service.automobile.R
import com.car.service.automobile.Resource
import com.car.service.automobile.main.MainActivity
import com.car.service.automobile.main.MainViewModel
import com.car.service.automobile.model.Result
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * A simple [Fragment] subclass.
 */
class RequestFragment : Fragment() {
    lateinit var mainViewModel: MainViewModel
    private val args: RequestFragmentArgs by navArgs()
    private lateinit var workshopData: Result

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel = (activity as MainActivity).mainViewModel

        workshopData = args.workshopData

        requestWorkshop()
    }

    private fun requestWorkshop() {

        CoroutineScope(Dispatchers.IO).launch {

            mainViewModel.requestWorkshop(workshopData.workshopID)

            withContext(Dispatchers.Main) {
                mainViewModel.workshop.observe(viewLifecycleOwner, Observer { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            pb.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            pb.visibility = View.INVISIBLE
                            resource.data.let {
                                if (it != null) {
                                    workshopName.text = it.name
                                    workshopRate.text = it.CompanyReview.toString()
                                    phoneNumber.text = it.contacts
                                } else {
                                    Snackbar.make(
                                        fragment_request,
                                        "Hakuna Garage ya karibu",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        is Resource.Error -> {
                            pb.visibility=View.INVISIBLE
                            resource.message.let {
                                Snackbar.make(fragment_request, "$it", Snackbar.LENGTH_LONG).show()

                            }
                        }
                    }
                })
            }
        }
    }
}
