package com.smeds.inframe.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smeds.inframe.model.Device
import android.widget.TextView
import com.smeds.inframe.R


class DeviceAdapter(context : Context, devices : ArrayList<Device>) :
    RecyclerView.Adapter<DeviceAdapter.DeviceHolder>() {

    private var context : Context = context
    private var devices : ArrayList<Device> = devices

    // Adapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rvrow_devices, parent, false)
        return DeviceHolder(view)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        val device = devices.get(position)
        holder.setDetails(device)
    }


    // Holder
    class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtName: TextView
        private val txtDeviceInfo : TextView

        init {
            txtName = itemView.findViewById(R.id.txtName)
            txtDeviceInfo = itemView.findViewById(R.id.txtDeviceInfo)
        }

        fun setDetails(device : Device) {
           txtName.text = device.name
           txtDeviceInfo.text = device.deviceInfo.toString()
        }


    }



}