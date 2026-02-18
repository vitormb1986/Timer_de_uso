package com.timerdeuso.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.timerdeuso.app.R
import com.timerdeuso.app.data.model.AppInfo

class AppListAdapter(
    private val onToggle: (AppInfo, Boolean) -> Unit,
    private val onTimeTap: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
        val packageText: TextView = view.findViewById(R.id.app_package)
        val checkbox: CheckBox = view.findViewById(R.id.app_checkbox)
        val timeLimit: TextView = view.findViewById(R.id.app_time_limit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getItem(position)
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.appName
        holder.packageText.text = app.packageName

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = app.isMonitored
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onToggle(app, isChecked)
        }

        if (app.isMonitored) {
            holder.timeLimit.visibility = View.VISIBLE
            holder.timeLimit.text = "${app.timeLimitMinutes} min"
            holder.timeLimit.setOnClickListener { onTimeTap(app) }
        } else {
            holder.timeLimit.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(old: AppInfo, new: AppInfo) =
            old.packageName == new.packageName
        override fun areContentsTheSame(old: AppInfo, new: AppInfo) = old == new
    }
}
