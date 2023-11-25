package com.example.cuackeat.Data

import android.content.Context
import  com.example.cuackeat.R
import java.util.*

//import com.example.cuackeat.Utilities.ImageUtilities

//Esta clase esta manejada como un singleton
//Se genera una solo instancia durante toda el tiempo de ejecución

object DataManager {
    val tipos = ArrayList<Tipo>()
    val albums = ArrayList<Album>()

    init {
        this.initializeTipo()
        this.initializeAlbums()
    }

    private fun initializeTipo(){
        var tipo =  Tipo(1,"Mexicana")
        tipos.add(tipo)

        tipo = Tipo(2,"Parrilla")
        tipos.add(tipo)

        tipo = Tipo(3,"Café")
        tipos.add(tipo)

        tipo = Tipo(4,"Italiana")
        tipos.add(tipo)

        tipo = Tipo(5,"Mariscos")
        tipos.add(tipo)

    }

    private fun initializeAlbums(){
        var album =  Album()
        album.strTitle =  "il Cielo Italia"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles01,content!!)
        album.intIdImage = R.drawable.il_cielo_italia
        album.tipo =  tipos[4]

        albums.add(album)

        album =  Album()
        album.strTitle =  "Sierra Madre Brewing Co."
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles02,content!!)
        album.intIdImage = R.drawable.sierra_madre
        album.tipo =  tipos[0]

        albums.add(album)

        album =  Album()
        album.strTitle =  "El Rey del Cabrito"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles03,content!!)
        album.intIdImage = R.drawable.el_rey_del_cabrito
        album.tipo =  tipos[1]

        albums.add(album)

        album =  Album()
        album.strTitle =  "Super Salads"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles04,content!!)
        album.intIdImage = R.drawable.super_salad
        album.tipo =  tipos[0]

        albums.add(album)

        album =  Album()
        album.strTitle =  "Cenacolo"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles05,content!!)
        album.intIdImage = R.drawable.cenacolo
        album.tipo =  tipos[4]

        albums.add(album)

        album =  Album()
        album.strTitle =  "Cuerno Calzada"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles05,content!!)
        album.intIdImage = R.drawable.cuerno
        album.tipo =  tipos[2]

        albums.add(album)

        album =  Album()
        album.strTitle =  "Flama"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles05,content!!)
        album.intIdImage = R.drawable.flama
        album.tipo =  tipos[2]

        albums.add(album)

        album =  Album()
        album.strTitle =  "The Food Box"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles05,content!!)
        album.intIdImage = R.drawable.foodbox
        album.tipo =  tipos[0]

        albums.add(album)

        album =  Album()
        album.strTitle =  "Los Curricanes"
        album.strDescription = LOREMIPSUM
        //album.imgArray =  ImageUtilities.getByteArrayFromResourse(R.drawable.beatles05,content!!)
        album.intIdImage = R.drawable.curricanes
        album.tipo =  tipos[5]

        albums.add(album)

    }

}