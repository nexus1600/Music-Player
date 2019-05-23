package com.example.lenovo.echo

class CurrentSongHelper : CharSequence {
    override val length: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun get(index: Int): Char {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var  songArtist: String?=null
    var songTitle: String?=null
    var songPath: String?=null
    var songId: Long=0
    var currentPosition: Int=0
    var isPlaying: Boolean = false
    var isLoop: Boolean = false
    var isShuffle: Boolean = false
    var trackPosition: Int=0
}