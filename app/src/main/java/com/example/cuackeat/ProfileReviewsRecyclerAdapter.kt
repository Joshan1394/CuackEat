package com.example.cuackeat

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Data.ALBUM_POSITION
import com.example.cuackeat.Data.Album
import com.example.cuackeat.Data.ReviewObject
import com.example.cuackeat.Data.Restaurant
import com.example.cuackeat.Utilities.ImageUtilies

class ProfileReviewsRecyclerAdapter(val context:Context, var reviews:List<ReviewObject>):RecyclerView.Adapter<ProfileReviewsRecyclerAdapter.ViewHolder>(), Filterable {

    private  val layoutInflater =  LayoutInflater.from(context)
    private val fullAlbums =  ArrayList<ReviewObject>(reviews)

    //se hace cargo de los graficos
    inner class  ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{

        val txtUserNickname =  itemView?.findViewById<TextView>(R.id.txtReviewAlias)
        val txtTitle =  itemView?.findViewById<TextView>(R.id.txtReviewTitle)
        val txtRestaurantName =  itemView?.findViewById<TextView>(R.id.txtReviewRestaurantName)
        val txtDescription =  itemView?.findViewById<TextView>(R.id.txtReviewDescription)
        //val imgAlbumCard =  itemView?.findViewById<ImageView>(R.id.imgReviewCard)
        var albumPosition:Int =  0

        init{
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {

            when(v!!.id){
                R.id.idFrameLayoutCardReview->{
                    // Lanzamos el intent para abrir el Detalle de la Review
                    val  activityIntent =  Intent(context, ReviewDetailActivity::class.java)
                    activityIntent.putExtra(ALBUM_POSITION, reviews[this.albumPosition].id)
                    activityIntent.putExtra("author_user_id", reviews[this.albumPosition].user_id)
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
        // Definimos el layout del item para la lista de Reviews
        val itemView =  this.layoutInflater.inflate(R.layout.item_review_list,parent,false)
        return  ViewHolder(itemView)
    }

    override fun getItemCount(): Int  =  this.reviews.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Definir los textos de la review
        val album =  this.reviews[position]
        holder.txtTitle.text = album.reviewTitle

        var i= 0
        while (reviews[i].reviewTitle!=holder.txtTitle.text){
            i++
        }

        holder.txtDescription.setText(abbreviateString(album.reviewDescription, 100))
        holder.txtUserNickname.setText(abbreviateString(album.userNickname, 100))
        holder.txtRestaurantName.setText(abbreviateString(album.restaurantTitle, 100))

        holder.albumPosition =  i
        //holder.imgAlbumCard.setImageBitmap(ImageUtilities.getBitMapFromByteArray(album.imgArray!!))

        //if(album.imgArray == null && album.intIdImage != null){
        //    holder.imgAlbumCard.setImageResource(album.intIdImage!!)
        //}else if (album.imgArray != null){
        //    try {
        //        holder.imgAlbumCard.setImageBitmap(ImageUtilities.getBitMapFromByteArray(album.imgArray!!))
        //    } catch (e: Exception) {
        //        // Manejar la excepción
        //        Log.e("ERROR", "Ocurrió un error al intentar mostrar la imagen: ${e.message}")
        //    }
        //}

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



                    reviews.filter { album ->

                       album.reviewTitle!!.toLowerCase().contains(queryString) || album.reviewDescription!!.toLowerCase().contains(queryString) || album.restaurantTitle!!.toLowerCase().contains(queryString)
                   }
               }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                reviews =  results?.values as List<ReviewObject>
                notifyDataSetChanged()
            }

        }
    }
}