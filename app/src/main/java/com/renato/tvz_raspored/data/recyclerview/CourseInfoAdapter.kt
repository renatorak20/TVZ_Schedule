package com.renato.tvz_raspored.data.recyclerview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renato.tvz_raspored.R
import com.renato.tvz_raspored.data.model.CourseInfo
import com.renato.tvz_raspored.databinding.CourseInfoRecyclerItemBinding
import okhttp3.internal.concurrent.formatDuration
import java.text.SimpleDateFormat
import java.util.ArrayList

class CourseInfoAdapter(private val array: ArrayList<CourseInfo>, private val context: Context): RecyclerView.Adapter<CourseInfoAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder =
        CourseViewHolder(CourseInfoRecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), context)

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) = holder.bind(array[position])

    class CourseViewHolder(private val binding: CourseInfoRecyclerItemBinding, private val context: Context): RecyclerView.ViewHolder(binding.root) {
        fun bind(courseInfo: CourseInfo) {
            with(binding) {
                title.text = courseInfo.title
                lecturer.text = courseInfo.lecturer
                timePeriod.text = context.getString(R.string.periodTime, formatTimeToPeriod(courseInfo.start), formatTimeToPeriod(courseInfo.end))
                if(courseInfo.color.isNotEmpty()) background.setBackgroundColor(Color.parseColor(courseInfo.color))
            }
        }

        fun formatTimeToPeriod(time: String): String {
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val outputDateFormat = SimpleDateFormat("HH:mm")
            val date = inputDateFormat.parse(time)
            return outputDateFormat.format(date)
        }

    }

    override fun getItemCount() = array.size
}