package com.example.parstagram.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.parstagram.MainActivity
import com.example.parstagram.Post
import com.example.parstagram.PostAdapter
import com.example.parstagram.R
import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseQuery

open class HomeFragment : Fragment() {

    lateinit var rvPosts : RecyclerView
    lateinit var adapter: PostAdapter

    var allPosts: ArrayList<Post> = arrayListOf()

    lateinit var swipeContainer: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // This is where we set up our views and click listeners

        rvPosts = view.findViewById(R.id.rvPosts)

        swipeContainer = view.findViewById(R.id.swipeContainer)

        /**
         * Steps to populate RecyclerView
         * 1. Create layout for each row
         * 2. Create data source for each row (this is the Post class)
         * 3. Create adapter that will bridge data and row layout
         * 4. Set adapter on RecyclerView
         * 5. Set layout manager on RecyclerView
         */

        adapter = PostAdapter(requireContext(), allPosts)
        rvPosts.adapter = adapter

        rvPosts.layoutManager = LinearLayoutManager(requireContext())

        swipeContainer.setOnRefreshListener {
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            queryPosts()
        }
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        queryPosts()
    }

    // Query for all posts in the server
    open fun queryPosts() {

        // Specify which class to query
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        // Find all Post Objects
        query.include(Post.KEY_USER)
        query.addDescendingOrder("createdAt")
        query.findInBackground(object: FindCallback<Post> {
            override fun done(posts: MutableList<Post>?, e: ParseException?) {
                if (e != null) {
                    Log.e(TAG, "Error fetching posts")
                } else {
                    if (posts != null) {
//                        for (post in posts) {
//                            Log.i(
//                                TAG, "Post: " + post.getDescription() +
//                                    " , Username: " + post.getUser()?.username)
//                        }
                        adapter.clear()
                        allPosts.addAll(posts.subList(0, 20))
                        adapter.notifyDataSetChanged()
                        swipeContainer.setRefreshing(false)
                    }
                }
            }
        })
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}