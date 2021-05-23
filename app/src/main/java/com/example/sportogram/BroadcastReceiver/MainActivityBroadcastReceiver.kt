/**
Broadcast receiver who detects internet connection and shows TextView warning depending on the
 connectivity status.
 */

package com.example.sportogram.BroadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.view.View
import android.widget.TextView
import com.example.sportogram.Activity.MainActivity
import com.example.sportogram.Models.Post
import com.example.sportogram.R

class MainActivityBroadcastReceiver(
    private val mainActivity: MainActivity
) : BroadcastReceiver() {

    private var post: List<Post> = ArrayList()

    fun setPostList(postList: List<Post>) {
        post = postList
    }

    override fun onReceive(context: Context, p1: Intent?) {
        val conn = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conn.activeNetworkInfo
        //If there is internet connection hide warning
        if (networkInfo != null && (networkInfo.type == ConnectivityManager.TYPE_WIFI || networkInfo.type == ConnectivityManager.TYPE_MOBILE)) {
            mainActivity.findViewById<TextView>(R.id.NoInternetConnectionTextView).visibility =
                View.GONE
        }
        //If there is no internet show warning only if we are already in a loaded activity with
        // posts, otherwise another different warning is shown
        else {
            if (post.isNotEmpty()) {
                mainActivity.findViewById<TextView>(R.id.NoInternetConnectionTextView).visibility =
                    View.VISIBLE
            }
        }
    }
}