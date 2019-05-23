package com.example.lenovo.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.lenovo.echo.CurrentSongHelper
import com.example.lenovo.echo.R
import com.example.lenovo.echo.SongPlayingFragment
import com.example.lenovo.echo.Songs
import com.example.lenovo.echo.adapters.MainScreenAdapter
import com.example.lenovo.echo.fragments.MainScreeenFragment.Statified.mMediaPlayer
import java.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class MainScreeenFragment : Fragment() {
    var getSongsList: ArrayList<Songs>? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseHelper: CurrentSongHelper?=null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var _mainScreenAdapter: MainScreenAdapter? = null
    object Statified{
        var mMediaPlayer:MediaPlayer?= null
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_main_screeen, container, false)
        setHasOptionsMenu(true)
        visibleLayout = view?.findViewById<RelativeLayout>(R.id.visibleLayout)
        noSongs = view?.findViewById<RelativeLayout>(R.id.noSongs)
        nowPlayingBottomBar = view?.findViewById<RelativeLayout>(R.id.hiddenBarMainscreen)
        songTitle = view?.findViewById<TextView>(R.id.songTitleMainScreen)
        playPauseButton = view?.findViewById<ImageButton>(R.id.playPauseButton)
        recyclerView = view?.findViewById<RecyclerView>(R.id.contentMain)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList = getSongsFromPhone()
        val prefs = activity.getSharedPreferences("action_sort",Context.MODE_PRIVATE)
        val action_sort_ascending = prefs.getString("action_sort_ascending","true")
        val action_sort_recent = prefs.getString("action_sort_recent","false")
        if (getSongsList== null){
            visibleLayout?.visibility= View.INVISIBLE
            noSongs?.visibility=View.VISIBLE
        }else{
            _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(myActivity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
        }


        if (getSongsList != null){
            if (action_sort_ascending!!.equals("true",ignoreCase = true)){
                Collections.sort(getSongsList,Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }else if (action_sort_recent!!.equals("true",ignoreCase = true)){
                Collections.sort(getSongsList,Songs.Statified.dateComperator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        bottomBar_setup()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main,menu)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        if(switcher ==R.id.action_sort_ascending){
            val editor = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending","true")
            editor?.putString("action_sort_recent","false")
            editor?.apply()
            if (getSongsList!= null){
                Collections.sort(getSongsList,Songs.Statified.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }else if (switcher == R.id.action_sort_recent){
            val editortwo = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editortwo?.putString("action_sort_recent","true")
            editortwo?.putString("action_sort_ascending","false")
            editortwo?.apply()
            if (getSongsList!= null){
                Collections.sort(getSongsList,Songs.Statified.dateComperator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
        }
        return arrayList
    }
    fun bottomBar_setup(){
        nowPlayingBottomBar?.isClickable = false
        bottomBarClickHandlers()
        try {
            songTitle?.text = SongPlayingFragment.Statified.currentSongHelper
            SongPlayingFragment.Statified.mediaplayer?.setOnCompletionListener({
                SongPlayingFragment.Staticated.onSongComplete()
                songTitle?.text = SongPlayingFragment.Statified.currentSongHelper
            })
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean) {

                playPauseHelper?.isPlaying = true
                nowPlayingBottomBar?.visibility = View.VISIBLE
                nowPlayingBottomBar?.layoutParams?.height = RecyclerView.LayoutParams.WRAP_CONTENT
                nowPlayingBottomBar?.setPadding(0, 11, 0, 11)
                nowPlayingBottomBar?.requestLayout()
            } else {
                playPauseHelper?.isPlaying = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun bottomBarClickHandlers() {

        nowPlayingBottomBar?.setOnClickListener({


            try {
                mMediaPlayer = SongPlayingFragment.Statified.mediaplayer
                val songPlayingFragment = SongPlayingFragment()
                val _fetch_from_Songs_Fragment = SongPlayingFragment.Statified.fetchSongs
                val args = Bundle()

                args.putString("BottomBar", "true")
                args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
                SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.let { it1 -> args.putInt("SongPosition", it1) }
                SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt()?.let { it1 -> args.putInt("SongId", it1) }
                args.putParcelableArrayList("songsData", _fetch_from_Songs_Fragment)
                songPlayingFragment.arguments = args
                fragmentManager?.beginTransaction()
                        ?.replace(R.id.details_fragment, songPlayingFragment)
                        ?.addToBackStack("MainScreenFragment")
                        ?.commit()
            } catch (e: Exception) {
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        })

        playPauseButton?.setOnClickListener({
            if (playPauseHelper?.isPlaying as Boolean) {
                SongPlayingFragment.Statified.mediaplayer?.pause()
                playPauseHelper?.trackPosition = SongPlayingFragment.Statified.mediaplayer?.getCurrentPosition()!!
                playPauseHelper?.isPlaying = false
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                SongPlayingFragment.Statified.mediaplayer?.seekTo((playPauseHelper as CurrentSongHelper).trackPosition)
                SongPlayingFragment.Statified.mediaplayer?.start()
                playPauseHelper?.isPlaying = true
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })

    }


}
