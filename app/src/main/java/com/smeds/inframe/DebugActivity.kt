package com.smeds.inframe

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DebugActivity : AppCompatActivity(), DebugActivityRecyclerViewAdapter.ItemClickListener {

    lateinit var recyclerViewAdapter : DebugActivityRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        // Ottieni tutte le activity...
        val activities = packageManager
            .getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities
        // ... e metti i nomi in una lista
        val activitiesList = ArrayList<String>()
        for (activity in activities) {
            activitiesList.add(activity.name)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.debugRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerViewAdapter = DebugActivityRecyclerViewAdapter(this, activitiesList)
        recyclerViewAdapter.setClickListener(this)
        recyclerView.adapter = recyclerViewAdapter

    }

    // Start intent quando il bottone viene premuto
    override fun onItemClick(view: View?, position: Int) {
        try {
            val c = Class.forName(recyclerViewAdapter.getItem(position))
            val intent = Intent(this, c)
            startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(this, "Could not start: ${ex.message}", Toast.LENGTH_SHORT).show()
        }

    }

}


// Adapter assolutamente non rubato da stackoverflow
class DebugActivityRecyclerViewAdapter internal constructor(context: Context?, data: ArrayList<String>) :
    RecyclerView.Adapter<DebugActivityRecyclerViewAdapter.ViewHolder>() {
    private val mData: List<String>
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.debug_rvrow, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = mData[position]
        holder.button.text = activity
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var button: Button = itemView.findViewById(R.id.tvActivityName)
        override fun onClick(view: View?) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
            button.setOnClickListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String {
        return mData[id]
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: DebugActivity) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    // data is passed into the constructor
    init {
        mInflater = LayoutInflater.from(context)
        mData = data
    }
}
