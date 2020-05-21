package com.dkarakaya.songify

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dkarakaya.songify.library.LibraryFragment
import com.dkarakaya.songify.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
        //added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container,
                    LibraryFragment()).commit()
        }
    }

    private fun render() {
        songTitle = cur_song_title
        songArtist = cur_song_artist
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
    }

    private val navListener: BottomNavigationView.OnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                lateinit var fragment: Fragment
                when (item.itemId) {
                    R.id.nav_lib -> fragment = LibraryFragment()
                    R.id.nav_search -> fragment = SearchFragment()
                }
                supportFragmentManager.beginTransaction().replace(R.id.container,
                        fragment).addToBackStack(null).commit()
                true
            }


    companion object {
        lateinit var songTitle: TextView
        lateinit var songArtist: TextView

        fun setPlayingSongDetails(title: String?, artist: String?) {
            songTitle.text = title
            songArtist.text = artist
        }
    }
}
