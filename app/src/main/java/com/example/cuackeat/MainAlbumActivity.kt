package com.example.cuackeat

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Data.ALBUM_POSITION
import com.example.cuackeat.Data.DEFAULT_ALBUM_POSITION
import com.example.cuackeat.Data.DataManager

class MainAlbumActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private var albumAdapter:AlbumRecycleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_search)
        setSupportActionBar(findViewById(R.id.toolbarAlbumDetail))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->

            val  activityIntent =  Intent(this,AlbumActivity::class.java)
            activityIntent.putExtra(ALBUM_POSITION,DEFAULT_ALBUM_POSITION)
            startActivity(activityIntent)
        }

        //RecyclerView
        val rcListAlbum = findViewById<RecyclerView>(R.id.rcListAlbum)
        rcListAlbum.layoutManager =  LinearLayoutManager(this)
        this.albumAdapter =  AlbumRecycleAdapter(this, DataManager.albums)
        rcListAlbum.adapter = this.albumAdapter

        //SearchView
        val vwSearchAlbum = findViewById<SearchView>(R.id.vwSearchAlbum)
        vwSearchAlbum.setOnQueryTextListener(this)
    }

    override fun onResume() {
        super.onResume()
        val rcListAlbum = findViewById<RecyclerView>(R.id.rcListAlbum)
        rcListAlbum.adapter?.notifyDataSetChanged()
    }

    //Search View
    override fun onQueryTextSubmit(query: String?): Boolean {

        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {

        if (newText != null){
            if(albumAdapter != null) this.albumAdapter?.filter?.filter(newText)
        }
        return false
    }
}