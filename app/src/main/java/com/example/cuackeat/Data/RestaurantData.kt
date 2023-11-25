package com.example.cuackeat.Data


//Las variables se declaran como opcionales.
//Esto permite crear un objecto de album por defecto vacio
//que sea usa cuando vamos agregar un nuevo album
class Restaurant(var id:Int? = null, var strTitle:String? = null, var strDescription:String? =  null, var strLocation:String? = null,  var intIdImage:Int? =  null, var imgArray:ByteArray? =  null){}