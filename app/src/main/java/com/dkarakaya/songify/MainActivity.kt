package com.dkarakaya.songify

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        render()

        //added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    LibraryFragment()).commit()
        }
    }

    private fun render() {
        tvCurSongTitle = findViewById(R.id.tvCurSongTitle)
        tvCurArtist = findViewById(R.id.tvCurSongArtist)
        lCurSongDetails = findViewById(R.id.curSongDetails)
        lCurSongDetails.visibility = View.GONE
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val navListener: BottomNavigationView.OnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                lateinit var fragment: Fragment
                when (item.itemId) {
                    R.id.nav_lib -> fragment = LibraryFragment()
                    R.id.nav_search -> fragment = SearchFragment()
                }
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                        fragment).commit()
                true
            }

    companion object {
        private lateinit var tvCurSongTitle: TextView
        private lateinit var tvCurArtist: TextView
        private lateinit var lCurSongDetails: LinearLayout

        @JvmStatic
        fun setCurSongDetails(curSongTitle: String?, curSongArtist: String?) {
            tvCurSongTitle.text = curSongTitle
            tvCurArtist.text = curSongArtist
            lCurSongDetails.visibility = View.VISIBLE
        }
    }
}
