package com.example.spyne

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spyne.RoomData.PictureModel
import com.example.spyne.databinding.CardImageBinding
import java.text.SimpleDateFormat
import java.util.Locale


class ImaagesAdapter(val items: ArrayList<PictureModel>,val itemClickListener: ImageItemClickListener) : RecyclerView.Adapter<ImaagesAdapter.ViewHolder>() {

    val inputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CardImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int { return items.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val binding: CardImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PictureModel) {

            Glide.with(binding.root.context).load(Uri.parse(item.imageUri)).into(binding.imagethumb)

            binding.upload.setOnClickListener {
                if(item.status == "uploading" || item.status == "cancelled")
                  itemClickListener.onPauseClicked(adapterPosition)
            }

            if(item.resultdate != item.selecteddate){
                binding.imageTitle.text = "Video was uploaded on "+ inputFormat.format(item.resultdate)
            }

            binding.status.text = inputFormat.format(item.selecteddate)

            if (item.status == "saved") {
                binding.upload.visibility = View.VISIBLE
                binding.upload.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.saved)
            } else if (item.status == "uploaded") {
                binding.upload.text = "Uploaded"
                binding.upload.backgroundTintList = ContextCompat.getColorStateList(binding.root.context,R.color.color_green)
            } else if (item.status == "uploading") {
                binding.upload.text = "Uploading"
                binding.upload.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.uploading)
            } else if (item.status == "cancelled") {
                binding.upload.text = "Resume"
                binding.upload.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.resume)
            } else {
                binding.upload.visibility = View.GONE
            }

        }
    }
}
