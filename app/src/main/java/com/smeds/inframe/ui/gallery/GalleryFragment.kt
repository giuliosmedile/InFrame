package com.smeds.inframe.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.smeds.inframe.R
import com.smeds.inframe.databinding.FragmentGalleryBinding
import com.smeds.inframe.home.LeaderHomeActivity
import com.smeds.inframe.model.Device
import com.smeds.inframe.model.User
import kotlinx.android.synthetic.*

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val deviceList = ArrayList<Device>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val recyclerView = binding.deviceRecyclerView //view?.findViewById<RecyclerView>(R.id.deviceRecyclerView)

        createListData(container)
        val adapter = DeviceAdapter(requireContext(), deviceList)
        recyclerView?.adapter = adapter

//        val textView: TextView = binding.textGallery
//        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        val recyclerView2 = binding.deviceRecyclerView
        galleryViewModel.recyclerView.observe(viewLifecycleOwner, Observer {
            recyclerView2.adapter = adapter
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun createListData(container : ViewGroup?) {
        val u1 = User("username", "email")
        val u2 = User("another", "posta")
        val windowManager = (activity as LeaderHomeActivity).windowManager
        val d1 = Device(u1, windowManager, requireContext())
        val d2 = Device(u2, windowManager, requireContext())

        deviceList.add(d1); deviceList.add(d2)
    }
}