package com.example.lenovo.echo.adapters

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.lenovo.echo.R
import com.example.lenovo.echo.SongPlayingFragment
import com.example.lenovo.echo.Songs

class FavoriteAdapter(_songDetails: ArrayList<Songs>, _context: Context): RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>() {
    var songDetails: ArrayList<Songs> ? = null
    var mContext: Context? = null
    var mediaPlayer: MediaPlayer?= null

    init {
        this.songDetails = _songDetails
        this.mContext = _context
        this.mediaPlayer= SongPlayingFragment.Statified.mediaplayer
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObject = songDetails?.get(position)
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.artist
        holder.contentHolder?.setOnClickListener (View.OnClickListener{
            try {
                if (mediaPlayer?.isPlaying()as Boolean){
                    mediaPlayer?.stop()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString( "songArtist", songObject?.artist)
            args.putString( "path", songObject?.songsData)
            args.putString( "songTitle", songObject?.songTitle)
            args.putInt("songId", songObject?.songID?.toInt() as Int)
            args.putInt("SongPosition",position)
            args.putParcelableArrayList("songData",songDetails)
            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragmentFavorite")
                    .commit()
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder{
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_mainscreen_adapter,parent,false)
        return MyViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        if (songDetails== null){
            return 0
        }else{
            return (songDetails as ArrayList<Songs>).size
        }
    }
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null
        init {
            trackTitle = view.findViewById<TextView>(R.id.trackTitle)
            trackArtist = view.findViewById<TextView>(R.id.trackArtist)
            contentHolder = view.findViewById<RelativeLayout>(R.id.contentRow)
        }
    }

}
