package com.example.parstagram

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepath.apps.restclienttemplate.TimeFormatter
import org.w3c.dom.Text

class PostAdapter(val context: Context, val posts: ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {
        val post = posts.get(position)
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun clear() {
        posts.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView
        val ivImage: ImageView
        val etDescription: TextView
        val tvCreatedAt: TextView


        init {
            tvUsername = itemView.findViewById(R.id.tvUsername)
            ivImage = itemView.findViewById(R.id.ivPostPicture)
            etDescription = itemView.findViewById(R.id.tvPostDescription)
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt)
        }

        fun bind(post: Post) {
            etDescription.text = post.getDescription().toString()
            tvUsername.text = post.getUser()?.username

            tvCreatedAt.text = TimeFormatter.getTimeDifference(post.createdAt.toString())

            Glide.with(itemView.context).load(post.getImage()?.url).into(ivImage)
        }
    }
}