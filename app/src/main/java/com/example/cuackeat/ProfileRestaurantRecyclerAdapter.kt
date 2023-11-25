package com.example.cuackeat

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Data.ALBUM_POSITION
import com.example.cuackeat.Data.Restaurant
import com.example.cuackeat.Utilities.ImageUtilies

class ProfileRestaurantRecyclerAdapter(val context:Context, var restaurants:List<Restaurant>):RecyclerView.Adapter<ProfileRestaurantRecyclerAdapter.ViewHolder>(), Filterable {

    private  val layoutInflater =  LayoutInflater.from(context)
    private val fullAlbums =  ArrayList<Restaurant>(restaurants)

    //se hace cargo de los graficos
    inner class  ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{

        val txtTitle =  itemView?.findViewById<TextView>(R.id.txtRestaurantTitle)
        val txtDescription =  itemView?.findViewById<TextView>(R.id.txtRestaurantDescription)
        val imgAlbumCard =  itemView?.findViewById<ImageView>(R.id.imgRestaurantCard)
        var albumPosition:Int =  0

        init{
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {

            when(v!!.id){
                R.id.idFrameLayoutCardRestaurant->{
                    // Lanzamos el intent para abrir el Detalle del Videojuego
                    val  activityIntent =  Intent(context, RestaurantDetailActivity::class.java)
                    activityIntent.putExtra(ALBUM_POSITION, restaurants[this.albumPosition].id)
                    context.startActivity(activityIntent)
                }
            }

        }
    }

    private fun abbreviateString(str: String?, maxLength: Int): String {
        if(str == null){
            return ""
        }
        if (str.length > maxLength) {
            return str.substring(0, maxLength - 3) + "..."
        } else {
            return str
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =  this.layoutInflater.inflate(R.layout.item_restaurant_list,parent,false)
        return  ViewHolder(itemView)
    }

    override fun getItemCount(): Int  =  this.restaurants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val album =  this.restaurants[position]
        holder.txtTitle.text = album.strTitle

        var i= 0
        while (restaurants[i].strTitle!=holder.txtTitle.text){
            i++
        }

        holder.txtDescription.setText(abbreviateString(album.strDescription, 100))
        holder.albumPosition =  i
        //holder.imgAlbumCard.setImageBitmap(ImageUtilities.getBitMapFromByteArray(album.imgArray!!))

        if(album.imgArray == null && album.intIdImage != null){
            holder.imgAlbumCard.setImageResource(album.intIdImage!!)
        }else if (album.imgArray != null){
            try {
                holder.imgAlbumCard.setImageBitmap(ImageUtilities.getBitMapFromByteArray(album.imgArray!!))
            } catch (e: Exception) {
                // Manejar la excepción
                Log.e("ERROR", "Ocurrió un error al intentar mostrar la imagen: ${e.message}")
            }
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

                    // Filtramos por Título, Descripción del Videojuego
                    restaurants.filter { album ->

                       album.strTitle!!.toLowerCase().contains(queryString)|| album.strDescription!!.toLowerCase().contains(queryString)
                   }
               }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                restaurants =  results?.values as List<Restaurant>
                notifyDataSetChanged()
            }

        }
    }
}