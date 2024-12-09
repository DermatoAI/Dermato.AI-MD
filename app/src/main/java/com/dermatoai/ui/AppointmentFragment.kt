package com.dermatoai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dermatoai.databinding.FragmentAppointmentBinding
import com.dermatoai.helper.FinishedAppointmentListAdapter
import com.dermatoai.model.AppointmentData
import com.dermatoai.model.AppointmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AppointmentFragment : Fragment() {
    private lateinit var binding: FragmentAppointmentBinding
    private val viewModel: AppointmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val finishedListAdapter = FinishedAppointmentListAdapter()
        binding.finishedContainerList.apply {
            adapter = finishedListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.historyAppointment.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.apply {
                    finishedContainerList.visibility = GONE
                    historyEmptyAnima.visibility = VISIBLE
                }
            } else {
                binding.apply {
                    finishedContainerList.visibility = VISIBLE
                    historyEmptyAnima.visibility = GONE
                }
            }
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val finishedList = listOf(
            AppointmentData(
                date = dateFormat.parse("2024-12-01 09:00") ?: Date(),
                doctor = "Dr. John Smith"
            ),
            AppointmentData(
                date = dateFormat.parse("2024-12-02 14:30") ?: Date(),
                doctor = "Dr. Sarah Connor"
            ),
            AppointmentData(
                date = dateFormat.parse("2024-12-03 11:00") ?: Date(),
                doctor = "Dr. Emily Davis"
            ),
            AppointmentData(
                date = dateFormat.parse("2024-12-04 16:00") ?: Date(),
                doctor = "Dr. Michael Brown"
            ),
            AppointmentData(
                date = dateFormat.parse("2024-12-05 10:15") ?: Date(),
                doctor = "Dr. Anna Garcia"
            ),
            AppointmentData(
                date = dateFormat.parse("2024-12-06 13:45") ?: Date(),
                doctor = "Dr. David Wilson"
            )
        )
//        finishedListAdapter.submitList(finishedList)
    }

}