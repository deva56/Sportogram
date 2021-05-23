/** Application main activity. Observes data changes through ViewModel and acts accordingly.
Shows error if present. Contains BroadcastReceiver to listen for internet connectivity issues. */

package com.example.sportogram.Activity

import android.content.DialogInterface
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportogram.BroadcastReceiver.MainActivityBroadcastReceiver
import com.example.sportogram.Constants.Constants.Companion.full_url
import com.example.sportogram.Models.Post
import com.example.sportogram.R
import com.example.sportogram.RecyclerViewClasses.MainActivityRecyclerViewAdapter
import com.example.sportogram.RecyclerViewClasses.VideoPlayerRecyclerView
import com.example.sportogram.Viewmodel.MainActivityViewModel
import com.example.sportogram.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainActivityRecyclerViewAdapter
    private val posts: List<Post> = ArrayList()
    private lateinit var recyclerView: VideoPlayerRecyclerView
    private lateinit var receiver: MainActivityBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        recyclerView = binding.MainRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = MainActivityRecyclerViewAdapter(posts, this)
        recyclerView.adapter = adapter

        val mainActivityViewModel: MainActivityViewModel by viewModels()

        binding.UpdatingFeedTextView.visibility = View.VISIBLE
        binding.MainProgressBar.visibility = View.VISIBLE
        mainActivityViewModel.getFeed(full_url)

        mainActivityViewModel.getPostLiveData().observe(this, { posts ->
            binding.ErrorImageView.visibility = View.GONE
            binding.ErrorText.visibility = View.GONE
            binding.RefreshButton.visibility = View.GONE
            binding.MainProgressBar.visibility = View.GONE
            binding.UpdatingFeedTextView.visibility = View.GONE
            adapter.setRecords(posts)
            recyclerView.setPosts(posts)
            receiver.setPostList(posts)
        })

        mainActivityViewModel.getErrorLiveData().observe(this, {
            binding.MainProgressBar.visibility = View.GONE
            binding.UpdatingFeedTextView.visibility = View.GONE
            binding.ErrorImageView.visibility = View.VISIBLE
            binding.ErrorText.visibility = View.VISIBLE
            binding.RefreshButton.visibility = View.VISIBLE
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.errorDescription))
            builder.setTitle(getString(R.string.errorTitle))
            builder.setPositiveButton(
                getString(R.string.errorConfirmation)
            ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            val dialog = builder.create()
            dialog.show()
        })

        binding.RefreshButton.setOnClickListener {
            binding.ErrorImageView.visibility = View.GONE
            binding.ErrorText.visibility = View.GONE
            binding.RefreshButton.visibility = View.GONE
            binding.MainProgressBar.visibility = View.VISIBLE
            binding.UpdatingFeedTextView.visibility = View.VISIBLE
            mainActivityViewModel.getFeed(full_url)
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        receiver = MainActivityBroadcastReceiver(this)
        this.registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        recyclerView.releasePlayer()
    }
}