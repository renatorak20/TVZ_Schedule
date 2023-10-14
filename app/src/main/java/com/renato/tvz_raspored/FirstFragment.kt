package com.renato.tvz_raspored

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.renato.tvz_raspored.data.model.Department
import com.renato.tvz_raspored.databinding.FragmentFirstBinding
import com.renato.tvz_raspored.viewmodel.TestVM
import java.util.ArrayList

class FirstFragment : Fragment() {

    private var binding: FragmentFirstBinding? = null
    private lateinit var viewModel: TestVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TestVM::class.java]
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAvailableDepartments()
    }


}