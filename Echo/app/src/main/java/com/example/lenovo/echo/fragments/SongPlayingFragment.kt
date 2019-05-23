package com.example.lenovo.echo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.lenovo.echo.databases.EchoDatabase
import com.example.lenovo.echo.fragments.NexusFavoriteFragment
import com.example.lenovo.echo.R
import java.util.*
import java.util.concurrent.TimeUnit

class SongPlayingFragment : Fragment() {

    @SuppressLint("StaticFieldLeak")
    object Statified {
        var myActivity: Activity? = null
        var mediaplayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var seekBar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var shuffleImageButton: ImageButton? = null

        var currentSongHelper: CurrentSongHelper? = null
        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var fab: ImageButton? = null

        var favoriteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaplayer?.currentPosition
                startTimeText?.setText(String.format("%d:%d",

                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) -
                                TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long))))
                seekBar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }


        }
    }

    object Staticated {
        var MY_PREFS_SHUFFLE = "shuffle feature"
        var MY_PREFS_LOOP = "loop feature"
        fun onSongComplete() {
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("playingNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            } else {
                if (Statified.currentSongHelper?.isLoop as Boolean) {

                    Statified.currentSongHelper?.isPlaying = true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songPath = nextSong?.songsData
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long
                    updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
                    Statified.mediaplayer?.reset()
                    try {
                        Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaplayer?.prepare()
                        Statified.mediaplayer?.start()
                        processInformation(Statified.mediaplayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    playNext("playNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songtitle: String, songArtist: String) {
            var songTitleUpdated = songtitle
            var songArtistUpdated = songArtist
            if (songtitle.equals("<unknown>", true)) {
                songTitleUpdated = "unknown"
            }
            if (songArtist.equals("<unknown>", true)) {
                songArtistUpdated = "unknown"
            }

            Statified.songTitleView?.setText(songtitle)
            Statified.songArtistView?.setText(songArtist)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekBar?.max = finalTime
            Statified.startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                            TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
            )
            Statified.endTimeText?.setText(String.format("%d:%d",

                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                            TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )
            Statified.seekBar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }

        fun playNext(check: String) {
            if (check.equals("playNextNormal", true)) {
                Statified.currentPosition = Statified.currentPosition + 1
            } else if (check.equals("playingNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition

            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songPath = nextSong?.songsData
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Statified.currentSongHelper?.songId = nextSong?.songID as Long

            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
            Statified.mediaplayer?.reset()

            try {
                Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaplayer?.prepare()
                Statified.mediaplayer?.start()
                processInformation(Statified.mediaplayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
            }
        }
    }

    var mAccelaration: Float = 0f
    var mAccelarationCurrent: Float = 0f
    var mAccelarationLast: Float = 0f


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)

        setHasOptionsMenu(true)
        activity.title = "Now Playing"
        Statified.seekBar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favoriteIcon)
        Statified.fab?.alpha = 0.8f
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener,
                Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        Statified.audioVisualization?.onPause()
        super.onPause()

        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroy() {
        Statified.audioVisualization?.release()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelaration = 0.0f
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        mAccelarationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favoriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments.getString("path")
            _songTitle = arguments.getString("songTitle")
            _songArtist = arguments.getString("songArtist")
            songId = arguments.getInt("SongId").toLong()
            Statified.currentPosition = arguments.getInt("songPositions")
            Statified.fetchSongs = arguments.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavBottomBar = arguments.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
            Statified.mediaplayer = NexusFavoriteFragment.Statified.mediaPlayer
        } else {

            Statified.mediaplayer = MediaPlayer()
            Statified.mediaplayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaplayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaplayer?.start()
        }
        Staticated.processInformation(Statified.mediaplayer as MediaPlayer)
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaplayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }

        clickHandler()
        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {

            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            Statified.currentSongHelper?.isLoop = false
        }
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
        }
    }


    fun clickHandler() {
        Statified.fab?.setOnClickListener({
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
                Statified.favoriteContent?.deleteFavorite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Remove from favorites", Toast.LENGTH_SHORT).show()
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
                Statified.favoriteContent?.storeAsFavorite(Statified.currentSongHelper?.songId?.toInt(), Statified.currentSongHelper?.songArtist,
                        Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextNormalShuffle")
            } else {
                Staticated.playNext("PlayNextNormal")
            }
        })
        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        Statified.loopImageButton?.setOnClickListener({
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            } else {
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }
        })
        Statified.playPauseImageButton?.setOnClickListener({
            if (Statified.mediaplayer?.isPlaying as Boolean) {
                Statified.mediaplayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaplayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })

    }

    fun playPrevious() {
        Statified.currentPosition = Statified.currentPosition - 1
        if (Statified.currentPosition == -1) {
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songPath = nextSong?.songsData
        Statified.currentSongHelper?.currentPosition = Statified.currentPosition
        Statified.currentSongHelper?.songId = nextSong?.songID as Long
        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
        Statified.mediaplayer?.reset()
        try {
            Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaplayer?.prepare()
            Statified.mediaplayer?.start()
            Staticated.processInformation(Statified.mediaplayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
        }
    }


            fun bindShakeListener() {
                Statified.mSensorListener = object : SensorEventListener {
                    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                    }

                    override fun onSensorChanged(p0: SensorEvent) {
                        val x = p0?.values[0]
                        val y = p0?.values[1]
                        val z = p0?.values[2]

                        mAccelarationLast = mAccelarationCurrent
                        mAccelarationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                        val delta = mAccelarationCurrent - mAccelarationLast
                        mAccelaration = mAccelaration * 0.9f + delta

                        if (mAccelaration > 12) {
                            val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                            val isAllowed = prefs?.getBoolean("feature", false)
                            if (isAllowed as Boolean) {
                                Staticated.playNext("PlayNextNormal")
                            }
                        }

                    }
                }


            }

}