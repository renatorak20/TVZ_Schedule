package com.renato.tvz_raspored.data.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renato.tvz_raspored.data.model.CourseInfo
import com.renato.tvz_raspored.databinding.CourseInfoRecyclerItemBinding
import java.util.ArrayList

class CourseInfoAdapter(private val array: ArrayList<CourseInfo>): RecyclerView.Adapter<CourseInfoAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder =
        CourseViewHolder(CourseInfoRecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) = holder.bind(array[position])

    class CourseViewHolder(private val binding: CourseInfoRecyclerItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(courseInfo: CourseInfo) {
            with(binding) {
                title.text = courseInfo.title
                lecturer.text = courseInfo.lecturer
            }
        }
    }

    override fun getItemCount() = array.size
}