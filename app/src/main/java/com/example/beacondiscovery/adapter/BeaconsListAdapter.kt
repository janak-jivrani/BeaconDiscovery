package com.example.beacondiscovery.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beacondiscovery.models.BeaconsCsvDataModel
import java.io.Serializable

class BeaconsListAdapter(
    var mActivity: Activity,
    var mArrayList: ArrayList<BeaconsCsvDataModel>,
) : RecyclerView.Adapter<BeaconsListAdapter.MyViewHolder>(), Serializable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(mActivity).inflate(android.R.layout.simple_list_item_1, null)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        mArrayList[position].let {
            holder.beaconName.text = "UUID: ${it.uuid}\nMajor: ${it.major}\nMinor: ${it.minor}"
        }
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var beaconName: TextView

        init {
            beaconName = itemView.findViewById(android.R.id.text1)
        }
    }

}
