/**
Custom RecyclerView from third party. Original code is written in Java, here rewritten to Kotlin
because of better type safety and some additional requirements.
This class contains all the logic for displaying video posts.
Each video is played in ExoPlayer and dynamically added PlayerView.
Various calculations are made to determine which post is currently being viewed and which video
should be played, and what is previous video that should be removed and cleaned.
 */

package com.example.sportogram.RecyclerViewClasses

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sportogram.Models.Post
import com.example.sportogram.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

class VideoPlayerRecyclerView : RecyclerView {

    private enum class VolumeState {
        ON, OFF
    }

    //UI components
    private var thumbnail: ImageView? = null
    private var volumeControl: ImageView? = null
    private lateinit var progressBar: ProgressBar
    private var viewHolderParent: View? = null
    private lateinit var frameLayout: FrameLayout
    private lateinit var videoSurfaceView: PlayerView
    private lateinit var videoPlayer: SimpleExoPlayer

    //Various config variables
    private var posts: List<Post> = ArrayList()
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var isVideoViewAdded = false

    //Volume control variable(on or off)
    private lateinit var volumeState: VolumeState

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }


    //Initialization function, takes care of initial setup of view, creating ExoPlayer instance,
    // adding listeners for scrolling detection for later calculations and other
    private fun init() {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y
        videoSurfaceView = PlayerView(context)
        videoSurfaceView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        // 2. Create the player
        videoPlayer = SimpleExoPlayer.Builder(context).build()
        // Bind the player to the view.
        videoSurfaceView.useController = false
        videoSurfaceView.player = videoPlayer
        setVolumeControl(VolumeState.ON)
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    if (thumbnail != null) {
                        thumbnail?.visibility = VISIBLE
                    }

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true)
                    } else {
                        playVideo(false)
                    }
                }
            }

        })
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}
            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent == view) {
                    resetVideoView()
                }
            }
        })
        videoPlayer.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {}
            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        progressBar.visibility = VISIBLE
                    }
                    Player.STATE_ENDED -> {
                        videoPlayer.seekTo(0)
                    }
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_READY -> {
                        progressBar.visibility = GONE
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPlayerError(error: ExoPlaybackException) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
            override fun onSeekProcessed() {}
        })
    }

    //Takes care of calculating which post is currently viewed and which video should be played
    // or killed and resources cleaned
    fun playVideo(isEndOfList: Boolean) {
        val targetPosition: Int
        if (!isEndOfList) {
            val startPosition =
                (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
            var endPosition =
                (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }

            // if there is more than 1 list-item on the screen
            targetPosition = if (startPosition != endPosition) {
                val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)
                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }
        } else {
            targetPosition = posts.size - 1
        }

        // video is already playing so return
        if (targetPosition == playPosition) {
            return
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition

        // remove any old surface views from previously playing videos
        videoSurfaceView.visibility = INVISIBLE
        removeVideoView(videoSurfaceView)

        val currentPosition =
            targetPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        val holder = child.tag as MainActivityHolder
        thumbnail = holder.postThumbnail
        progressBar = holder.postProgressBar
        volumeControl = holder.postVolumeControl
        viewHolderParent = holder.itemView
        frameLayout = holder.itemView.findViewById(R.id.PostFrameLayout)
        videoSurfaceView.player = videoPlayer
        viewHolderParent!!.setOnClickListener(videoViewClickListener)
        val mediaItem = MediaItem.fromUri(posts[targetPosition].video.url)
        videoPlayer.setMediaItem(mediaItem)
        videoPlayer.prepare()
        videoPlayer.playWhenReady = true
    }

    //OnClickListener for volume toggle(on or off)
    private val videoViewClickListener = OnClickListener { toggleVolume() }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    // Remove the old player
    private fun removeVideoView(videoView: PlayerView) {
        val parent = videoView.parent as ViewGroup? ?: return

        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
            viewHolderParent!!.setOnClickListener(null)
        }
    }

    //Add view dynamically where ExoPlayer instance video will be played
    private fun addVideoView() {
        frameLayout.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView.requestFocus()
        videoSurfaceView.visibility = VISIBLE
        videoSurfaceView.alpha = 1f
        thumbnail?.visibility = GONE
    }

    //Clears old VideoView and brings video poster on top again to replace the video playback
    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView.visibility = INVISIBLE
            thumbnail?.visibility = VISIBLE
        }
    }

    //Clean resources and kill ExoPlayer instance
    fun releasePlayer() {
        videoPlayer.release()
    }

    //Turn volume on or off
    private fun toggleVolume() {
        if (volumeState == VolumeState.OFF) {
            setVolumeControl(VolumeState.ON)
        } else if (volumeState == VolumeState.ON) {
            setVolumeControl(VolumeState.OFF)
        }
    }

    private fun setVolumeControl(state: VolumeState) {
        volumeState = state
        if (state == VolumeState.OFF) {
            videoPlayer.volume = 0f
            animateVolumeControl()
        } else if (state == VolumeState.ON) {
            videoPlayer.volume = 1f
            animateVolumeControl()
        }
    }

    //Animating sound drawable to fade in and out smoothly
    private fun animateVolumeControl() {
        if (volumeControl != null) {
            volumeControl?.bringToFront()
            if (volumeState == VolumeState.OFF) {
                Glide.with(context!!).load(R.drawable.ic_no_sound)
                    .into(volumeControl!!)
            } else if (volumeState == VolumeState.ON) {
                Glide.with(context!!).load(R.drawable.ic_sound)
                    .into(volumeControl!!)
            }
            volumeControl?.animate()?.cancel()
            volumeControl?.alpha = 1f
            volumeControl?.animate()
                ?.alpha(0f)
                ?.setDuration(600)?.startDelay = 1000
        }
    }

    fun setPosts(posts: List<Post>) {
        this.posts = posts
    }
}
