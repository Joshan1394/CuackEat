package com.example.cuackeat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cuackeat.Data.*
import com.example.cuackeat.Utilities.ImageUtilies
import java.io.ByteArrayOutputStream

class AlbumActivity: AppCompatActivity(), View.OnClickListener {
    var albumPosition:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        val toolbarAlbumDetail = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAlbumDetail)
        setSupportActionBar(toolbarAlbumDetail)

        val btnOpenPhotoReel = findViewById<ImageButton>(R.id.btnOpenPhotoReel)
        btnOpenPhotoReel.setOnClickListener(this)

        //Spinner
        val adapterGenre =  ArrayAdapter<Tipo>(this,android.R.layout.simple_spinner_item, DataManager.tipos)
        adapterGenre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)
        spinnerGenre.adapter =  adapterGenre

        this.albumPosition =  savedInstanceState?.getInt(ALBUM_POSITION, DEFAULT_ALBUM_POSITION) ?: intent.getIntExtra(ALBUM_POSITION,DEFAULT_ALBUM_POSITION)
        //Display Album
        if(this.albumPosition != DEFAULT_ALBUM_POSITION){
            this.displayAlbum()
        }else{
            DataManager.albums.add(Album())
            this.albumPosition =  DataManager.albums.lastIndex
        }
    }

    override fun onPause() {
        super.onPause()
        this.saveAlbum()
    }

    //DISPLAY
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ALBUM_POSITION,this.albumPosition)

    }

    private fun saveAlbum() {
        val albumEdit:Album =  DataManager.albums[albumPosition]
        val lblNameAlbum = findViewById<TextView>(R.id.lblNameAlbum)
        val lblDesciption = findViewById<TextView>(R.id.lblDesciption)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)

        albumEdit.strTitle = lblNameAlbum.text.toString()
        albumEdit.strDescription =  lblDesciption.text.toString()
        albumEdit.tipo =  spinnerGenre.selectedItem as Tipo
    }

    private fun moveNext():Boolean{

        ++albumPosition
        this.displayAlbum()
        invalidateOptionsMenu()
        return true
    }

    private fun displayAlbum(){
        val albumEdit:Album =  DataManager.albums[albumPosition]
        val lblNameAlbum = findViewById<TextView>(R.id.lblNameAlbum)
        val lblDesciption = findViewById<TextView>(R.id.lblDesciption)
        val imgAlbum = findViewById<ImageView>(R.id.imgAlbum)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)

        lblNameAlbum.text = albumEdit.strTitle
        lblDesciption.setText(albumEdit.strDescription)

        if(albumEdit.imgArray == null){
            imgAlbum.setImageResource(albumEdit.intIdImage!!)
        }else{
            imgAlbum.setImageBitmap(ImageUtilities.getBitMapFromByteArray(albumEdit.imgArray!!))
        }


        val genrePosition =  DataManager.tipos.indexOf(albumEdit.tipo)
        spinnerGenre.setSelection(genrePosition)
    }

    //MENU
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        return when(item.itemId){
            R.id.itemActionEdit_MenuMain -> this.moveNext()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean{
        if(albumPosition >= DataManager.albums.lastIndex){
            val menuItem =  menu?.findItem(R.id.itemActionEdit_MenuMain)
            if(menuItem != null){
                menuItem.setIcon(R.drawable.ic_block_24)
                menuItem.isEnabled =  false
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    //CLICK
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnOpenPhotoReel->changeImage()
        }
    }

    //GALERIA DE IMAGENES
    private fun pickImageFromGallery() {
        //Abrir la galería
        val intent  =  Intent()
        intent.setAction(Intent.ACTION_PICK);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.type = "image/*"
        //startActivityForResult(Intent.createChooser(intent,"Selecciona"), IMAGE_PICK_CODE)
        startActivityForResult(intent, IMAGE_PICK_CODE)

    }

    private fun changeImage(){
        //check runtime permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            var boolDo:Boolean =  false
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE)
            }
            else{
                //permission already granted
                boolDo =  true

            }


            if(boolDo == true){
                pickImageFromGallery()
            }

        }

    }

    @SuppressLint("MissingSuperCall") //evita el missing supercall resultado
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray  ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //PERMISO DENEGADO
                    Toast.makeText(this, getString(R.string.Permission_denied).toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestcode: Int, resultcode: Int, data: Intent?) {
        super.onActivityResult(requestcode, resultcode, intent)

        if (resultcode == Activity.RESULT_OK) {
            //RESPUESTA DE LA CÁMARA CON TIENE LA IMAGEN
            if (requestcode == CAMERA_CODE) {

                val photo =  data?.extras?.get("data") as Bitmap
                val stream = ByteArrayOutputStream()
                //Bitmap.CompressFormat agregar el formato desado, estoy usando aqui jpeg
                photo.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                //Agregamos al objecto album el arreglo de bytes
                val albumEdit:Album =  DataManager.albums[albumPosition]
                albumEdit.imgArray =  stream.toByteArray()
                //Mostramos la imagen en la vista
                val imgAlbum = findViewById<ImageView>(R.id.imgAlbum)
                imgAlbum.setImageBitmap(photo)

            }

            if(requestcode == IMAGE_PICK_CODE){
                val imgAlbum = findViewById<ImageView>(R.id.imgAlbum)
                imgAlbum.setImageURI(data?.data)
                val bitmap = (imgAlbum.getDrawable() as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val albumEdit:Album =  DataManager.albums[albumPosition]
                albumEdit.imgArray =  baos.toByteArray()
            }
        }
    }
}