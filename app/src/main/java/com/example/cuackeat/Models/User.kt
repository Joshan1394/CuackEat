package com.example.cuackeat.Models

//esto objectos fueron generados con el plugin Json to Kotly Class
//Estos objecto se usa para pasar la respuesta recibida

//Las variables se declaran como opcionales.
//Esto permite crear un objecto de album por defecto vacio
//que sea usa cuando vamos agregar un nuevo album
data class User(var id:Int? =  null,
                var email:String? = null,
                var password:String? = null,
                var password_confirmation:String? =  null,
                var nickname:String? =  null,
                var description:String? =  null,
                var name:String? =  null,
                var last_name:String? =  null,
                var address:String? =  null,
                var phone:String? =  null,
                var image:String? =  null)
{
    // Clase Builder del Patrón Builder
    class Builder {
        private var id: Int? = null
        private var email: String? = null
        private var password: String? = null
        private var password_confirmation: String? = null
        private var nickname: String? = null
        private var description: String? = null
        private var name: String? = null
        private var last_name: String? = null
        private var address: String? = null
        private var phone: String? = null
        private var image: String? = null

        // Métodos para asignar los atributos
        fun id(id: Int?) = apply { this.id = id }
        fun email(email: String?) = apply { this.email = email }
        fun password(password: String?) = apply { this.password = password }
        fun password_confirmation(password_confirmation: String?) = apply { this.password_confirmation = password_confirmation }
        fun nickname(nickname: String?) = apply { this.nickname = nickname }
        fun description(description: String?) = apply { this.description = description }
        fun name(name: String?) = apply { this.name = name }
        fun last_name(last_name: String?) = apply { this.last_name = last_name }
        fun address(address: String?) = apply { this.address = address }
        fun phone(phone: String?) = apply { this.phone = phone }
        fun image(image: String?) = apply { this.image = image }

        // Método para construir y devolver una instancia de la clase que se está construyendo.
        fun build() = User(
            id,
            email,
            password,
            password_confirmation,
            nickname,
            description,
            name,
            last_name,
            address,
            phone,
            image
        )
    }
}


data class UserPassword(var old_password:String? =  null,
                        var new_password:String? = null,
                        var new_password_confirmation:String? = null){}


data class ResponseUser(val status: String?,
                        val data: User,
                        val message: Map<String, List<String>>?){}