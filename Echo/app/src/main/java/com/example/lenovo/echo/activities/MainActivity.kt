package com.example.lenovo.echo.activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.example.lenovo.echo.R
import com.example.lenovo.echo.SongPlayingFragment
import com.example.lenovo.echo.adapters.NavigationDrawerAdapter
import com.example.lenovo.echo.fragments.MainScreeenFragment

class MainActivity : AppCompatActivity() {
    object Statified{
        var drawerLayout: DrawerLayout?= null
        var notificationManager: NotificationManager?=null
    }
    var navigationDrawerIconList: ArrayList<String> = arrayListOf()
    var images_for_navdrawer = intArrayOf(R.drawable.navigation_allsongs,
            R.drawable.navigation_favorites,
            R.drawable.navigation_settings,
            R.drawable.navigation_aboutus)
    var trackNotificationBuilder:Notification?=null


    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)

        navigationDrawerIconList.add("All Songs")
        navigationDrawerIconList.add("Nexus Favorites")
        navigationDrawerIconList.add("Settings")
        navigationDrawerIconList.add("About Us")
        val _navigationAdaptor = NavigationDrawerAdapter(navigationDrawerIconList, images_for_navdrawer, this)
        _navigationAdaptor.notifyDataSetChanged()

        val toggle = ActionBarDrawerToggle(this@MainActivity, MainActivity.Statified.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        MainActivity.Statified.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()
        val mainScreenFragment = MainScreeenFragment()
        this.supportFragmentManager
                .beginTransaction()
                .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
                .commit()

        val _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconList, images_for_navdrawer, this)
        _navigationAdapter.notifyDataSetChanged()
        val navigation_recycler_view = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager = LinearLayoutManager(this)
        navigation_recycler_view.itemAnimator = DefaultItemAnimator()
        navigation_recycler_view.adapter = _navigationAdaptor
        navigation_recycler_view.setHasFixedSize(true)


        val intent = Intent(this@MainActivity,MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this@MainActivity,System.currentTimeMillis().toInt(),intent,0)
        trackNotificationBuilder= Notification.Builder(this)
                .setContentTitle("A track is playing in background")
                .setSmallIcon(R.drawable.echo_logo)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .build()
        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager


    }

    override fun onStart() {
        super.onStart()
        try {
            Statified.notificationManager?.cancel(1997)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean){
                Statified.notificationManager?.notify(1997, trackNotificationBuilder)
            }
        } catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            Statified.notificationManager?.cancel(1997)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}

