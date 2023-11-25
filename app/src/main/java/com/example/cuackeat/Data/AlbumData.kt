package com.example.cuackeat.Data


//Nos permite manejar los generos
class Tipo(var intId:Int, var strTitle:String){

    override fun toString(): String {
        return this.strTitle
    }
}
//Las variables se declaran como opcionales.
//Esto permite crear un objecto de album por defecto vacio
//que sea usa cuando vamos agregar un nuevo album
class Album(var strTitle:String? = null, var strDescription:String? =  null,  var intIdImage:Int? =  null, var tipo: Tipo? = null,  var imgArray:ByteArray? =  null){}