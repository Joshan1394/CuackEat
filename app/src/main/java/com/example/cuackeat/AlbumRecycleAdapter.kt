package com.example.cuackeat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Data.ALBUM_POSITION
import com.example.cuackeat.Data.Album
import com.example.cuackeat.Utilities.ImageUtilies

class AlbumRecycleAdapter(val context: Context, var albums:List<Album>): RecyclerView.Adapter<AlbumRecycleAdapter.ViewHolder>(),
    Filterable {

    private  val layoutInflater =  LayoutInflater.from(context)
    private val fullAlbums =  ArrayList<Album>(albums)

    //se hace cargo de los graficos
    inner class  ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{

        val txtTitle =  itemView?.findViewById<TextView>(R.id.txtTitle)
        val txtDescription =  itemView?.findViewById<EditText>(R.id.txtDescription)
        val imgAlbumCard =  itemView?.findViewById<ImageView>(R.id.imgAlbumCard)
        var albumPosition:Int =  0

        init{
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {

            when(v!!.id){
                R.id.idFrameLayoutCard->{
                    //Lanzamos el intent para abrir el detall
                    val  activityIntent =  Intent(context,AlbumActivity::class.java)
                    activityIntent.putExtra(ALBUM_POSITION,this.albumPosition)
                    context.startActivity(activityIntent)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =  this.layoutInflater.inflate(R.layout.item_album_list,parent,false)
        return  ViewHolder(itemView)
    }

    override fun getItemCount(): Int  =  this.albums.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val album =  this.albums[position]
        holder.txtTitle.text =  album.strTitle

        var i= 0
        while (fullAlbums[i].strTitle!=holder.txtTitle.text){
            i++
        }

        holder.txtDescription.setText(album.strDescription)
        holder.albumPosition =  i
        //holder.imgAlbumCard.setImageBitmap(ImageUtilities.getBitMapFromByteArray(album.imgArray!!))

        if(album.imgArray == null){
            holder.imgAlbumCard.setImageResource(album.intIdImage!!)
        }else{
            holder.imgAlbumCard.setImageBitmap(ImageUtilities.getBitMapFromByteArray(album.imgArray!!))
        }

    }

    //Busqueda del Adaptador
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(charSequence: CharSequence?): FilterResults {

                //Obtenemos la cadena
                val filterResults = Filter.FilterResults()
                filterResults.values =  if (charSequence == null || charSequence.isEmpty()){

                    fullAlbums

                }else{
                    val queryString = charSequence?.toString()?.toLowerCase()



                    albums.filter { album ->

                        album.strTitle!!.toLowerCase().contains(queryString)|| album.strDescription!!.toLowerCase().contains(queryString)
                    }
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                albums =  results?.values as List<Album>
                notifyDataSetChanged()
            }

        }
    }
}