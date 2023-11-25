package com.example.cuackeat

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cuackeat.Models.RestaurantModel
import com.example.cuackeat.Models.*

class SQLiteHelper(context:Context):SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /*Datos relevantes para la base de datos*/
    companion object{
        private const val DATABASE_VERSION=1
        private const val DATABASE_NAME="cuackeat.db"

        private const val TBL_USER = "users"
        //users
        private const val USER_ID="user_id"
        private const val EMAIL="email"
        private const val PASS="password"
        private const val NAME="name"
        private const val LAST_NAME="last_name"
        private const val PHONE="phone"
        private const val ADDRESS="address"
        private const val ALIAS="alias"
        private const val DESCRIPTION="description"
        private const val IMG="image"

        private const val TBL_RESTAURANTS = "restaurant"
        private const val TBL_RESTAURANTS_PROFILE = "restaurantProfile"
        private const val RESTAURANT_ID="restaurant_id"

        private const val TBL_REVIEWS = "reviews"
        private const val TBL_REVIEWS_PROFILE = "reviewsProfile"
        private const val TBL_REVIEWS_HOME = "reviewsHome"
        private const val TBL_REVIEWS_BORRADOR = "reviewsBorrador"
        private const val REVIEW_ID="review_id"
        private const val TITLE="title"
        private const val ALTA="created_at"
        private const val CAMBIO="updated_at"
        private const val BAJA="deleted_at"

        private const val TBL_FAV_RESTAURANTS = "favorite_restaurant"
        private const val FAV_RESTAURANT_ID="favorite_restaurant_id"

        private const val TBL_FAV_REVIEWS = "favorite_reviews"
        private const val FAV_REVIEW_ID="favorite_review_id"

        private const val TBL_REVIEW_IMAGES = "review_images"
        private const val TBL_REVIEW_IMAGES_BORR = "review_images_borr"
        private const val REVIEW_IMG_ID="review_image_id"
        private const val IMG_PATH="image_path"

        private const val TBL_REVIEWS_IMGS="review_images_detail"

    }

    //Crear tablas
    override fun onCreate(db:SQLiteDatabase?){
        val createTblUser= ("CREATE TABLE " + TBL_USER+ "("+
                                USER_ID+" BIGINT PRIMARY KEY,"+
                                EMAIL+ " TEXT," +
                                PASS + " TEXT, "+
                                NAME+" TEXT,"+
                                LAST_NAME+" TEXT,"+
                                PHONE+ " TEXT,"+
                                ADDRESS+" TEXT," +
                                ALIAS +" TEXT, "+
                                DESCRIPTION + " TEXT,"+
                                IMG+" TEXT)")
        db?.execSQL(createTblUser)

        val createTblRestaurants= ("CREATE TABLE " + TBL_RESTAURANTS+ "("+
                                    RESTAURANT_ID+" BIGINT PRIMARY KEY,"+
                                    NAME+ " TEXT," +
                                    DESCRIPTION + " TEXT, "+
                                    IMG+" TEXT)")
        db?.execSQL(createTblRestaurants)

        val createTblReviews= ("CREATE TABLE " + TBL_REVIEWS+ "("+
                                REVIEW_ID+" BIGINT PRIMARY KEY,"+
                                TITLE+ " TEXT," +
                                DESCRIPTION + " TEXT, "+
                                USER_ID+" BIGINT,"+
                                RESTAURANT_ID+" BIGINT,"+
                                ALTA +" TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "+
                                CAMBIO + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                                BAJA+" TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY ("+USER_ID+") REFERENCES "+TBL_USER+"("+USER_ID+"))")
        db?.execSQL(createTblReviews)

        //la tabla de reviews que se muestra en el perfil
        val createTblReviewsProfile= ("CREATE TABLE " + TBL_REVIEWS_PROFILE+ "("+
                REVIEW_ID+" BIGINT PRIMARY KEY,"+
                TITLE+ " TEXT," +
                DESCRIPTION + " TEXT, "+
                IMG+" TEXT,"+
                USER_ID+" BIGINT,"+
                ALIAS +" TEXT, "+
                RESTAURANT_ID + " BIGINT,"+
                NAME+" TEXT)")
        db?.execSQL(createTblReviewsProfile)

        val createTblFavRestaurants= ("CREATE TABLE " + TBL_FAV_RESTAURANTS+ "("+
                                        FAV_RESTAURANT_ID+" BIGINT PRIMARY KEY,"+
                                        USER_ID+ " BIGINT," +
                                        RESTAURANT_ID+" BIGINT,  FOREIGN KEY ("+USER_ID+") REFERENCES "+TBL_USER+"("+USER_ID+"), FOREIGN KEY ("+RESTAURANT_ID+") REFERENCES "+TBL_RESTAURANTS+"("+RESTAURANT_ID+"))")
        db?.execSQL(createTblFavRestaurants)

        val createTblFavReviews= ("CREATE TABLE " + TBL_FAV_REVIEWS+ "("+
                                    FAV_REVIEW_ID+" BIGINT PRIMARY KEY,"+
                                    USER_ID+ " BIGINT," +
                                    REVIEW_ID+" BIGINT,  FOREIGN KEY ("+ USER_ID +") REFERENCES "+TBL_USER+"("+USER_ID+"), FOREIGN KEY ("+REVIEW_ID+") REFERENCES "+TBL_REVIEWS+"("+REVIEW_ID+"))")
        db?.execSQL(createTblFavReviews)

        val createTblReviewImgs= ("CREATE TABLE " + TBL_REVIEW_IMAGES+ "("+
                                    REVIEW_IMG_ID+" INTEGER PRIMARY KEY,"+
                                    REVIEW_ID+ " BIGINT," +
                                    IMG_PATH+" TEXT, FOREIGN KEY ("+REVIEW_ID+") REFERENCES "+TBL_REVIEWS+"("+REVIEW_ID+"))")
        db?.execSQL(createTblReviewImgs)
    }

    //Si cambiamos de version
    override fun onUpgrade(db:SQLiteDatabase?, oldVersion: Int, newVersion: Int){
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_FAV_RESTAURANTS")
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_FAV_REVIEWS")
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_REVIEW_IMAGES")
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_REVIEWS")
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_REVIEWS_PROFILE")
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_USER")
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_RESTAURANTS")
        onCreate(db)
    }

    /*Insertar un estudiante*/
    fun insertUser(std: UserModel):Long{
        val db= this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(USER_ID, std.id)
        contentValues.put(EMAIL, std.email)
        contentValues.put(PASS, std.password)
        contentValues.put(NAME, std.name)
        contentValues.put(LAST_NAME, std.lastName)
        contentValues.put(PHONE, std.phone)
        contentValues.put(ADDRESS, std.address)
        contentValues.put(ALIAS, std.alias)
        contentValues.put(DESCRIPTION, std.description)
        contentValues.put(IMG, std.image)

        val success=db.insert(TBL_USER, null, contentValues)
        db.close()
        return success
    }

    fun truncateTable(): Int{

        val db= this.writableDatabase
        val success= db.delete(TBL_USER, "user_id>0", null)
        db.close()

        return success
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getAllUsers(): ArrayList<UserModel>{
        val stdList: ArrayList<UserModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_USER"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var email: String
        var password: String
        var name: String
        var lastname: String
        var phone: String
        var address: String
        var alias: String
        var description: String
        var image: String

        if(cursor.moveToFirst())
        {
            do{
                id=cursor.getInt(cursor.getColumnIndex("user_id"))
                email= cursor.getString(cursor.getColumnIndex("email"))
                password=cursor.getString(cursor.getColumnIndex("password"))
                name=cursor.getString(cursor.getColumnIndex("name"))
                lastname=cursor.getString(cursor.getColumnIndex("last_name"))
                phone=cursor.getString(cursor.getColumnIndex("phone"))
                address=cursor.getString(cursor.getColumnIndex("address"))
                alias=cursor.getString(cursor.getColumnIndex("alias"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                image=cursor.getString(cursor.getColumnIndex("image"))

                val std = UserModel(id,email,password, name, lastname, phone, address, alias, description, image)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    /*Insertar Restaurantes*/
    fun insertRestaurant(std: RestaurantModel):Long{
        val db= this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(RESTAURANT_ID, std.id)
        contentValues.put(NAME, std.name)
        contentValues.put(DESCRIPTION, std.description)
        contentValues.put(IMG, std.image)

        val success=db.insert(TBL_RESTAURANTS, null, contentValues)
        db.close()
        return success
    }

    fun truncateTableRestaurants(): Int{
        val db=this.writableDatabase

        val success= db.delete(TBL_RESTAURANTS, "restaurant_id>0", null)
        db.close()

        return success
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getAllRestaurants(): ArrayList<RestaurantModel>{
        val stdList: ArrayList<RestaurantModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_RESTAURANTS"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var name: String
        var description: String
        var image: String

        if(cursor.moveToFirst())
        {
            do{
                id=cursor.getInt(cursor.getColumnIndex("restaurant_id"))
                name=cursor.getString(cursor.getColumnIndex("name"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                image=cursor.getString(cursor.getColumnIndex("image"))

                val std = RestaurantModel(id, name, description, image)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    fun getRestaurant(id:Int): ArrayList<RestaurantModel>{
        val stdList: ArrayList<RestaurantModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_RESTAURANTS WHERE $RESTAURANT_ID=$id"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var name: String
        var description: String
        var image: String

        if(cursor.moveToFirst())
        {
            do{
                id=cursor.getInt(cursor.getColumnIndex("videogame_id"))
                name=cursor.getString(cursor.getColumnIndex("name"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                image=cursor.getString(cursor.getColumnIndex("image"))

                val std = RestaurantModel(id, name, description, image)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    fun getReview(id:Int): ArrayList<ReviewModel>{
        val stdList: ArrayList<ReviewModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_REVIEWS_HOME WHERE $REVIEW_ID=$id"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var title: String
        var description: String
        var userId: Int
        var restaurantId: Int

        if(cursor.moveToFirst())
        {
            do{
                id=cursor.getInt(cursor.getColumnIndex("review_id"))
                title=cursor.getString(cursor.getColumnIndex("title"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                userId=cursor.getInt(cursor.getColumnIndex("user_id"))
                restaurantId=cursor.getInt(cursor.getColumnIndex("restaurant_id"))

                val std = ReviewModel(id, title, description, userId, restaurantId)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    fun insertReviewsProfile(std: ReviewProfileModel):Long{
        val db= this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(REVIEW_ID, std.id)
        contentValues.put(TITLE, std.title)
        contentValues.put(DESCRIPTION, std.description)
        contentValues.put(IMG, std.images)
        contentValues.put(USER_ID, std.user_id)
        contentValues.put(ALIAS, std.user_nickname)
        contentValues.put(RESTAURANT_ID, std.restaurant_id)
        contentValues.put(NAME, std.restaurant_id)

        val success=db.insert(TBL_REVIEWS_PROFILE, null, contentValues)
        db.close()
        return success
    }

    fun truncateTableReviewsProfile(): Int{

        //la tabla de reviews que se muestra en el perfil
        val createTblReviewsProfile= ("CREATE TABLE IF NOT EXISTS " + TBL_REVIEWS_PROFILE+ "("+
                REVIEW_ID+" BIGINT PRIMARY KEY,"+
                TITLE+ " TEXT," +
                DESCRIPTION + " TEXT, "+
                IMG+" TEXT,"+
                USER_ID+" BIGINT,"+
                ALIAS +" TEXT, "+
                RESTAURANT_ID + " BIGINT,"+
                NAME+" TEXT)")

        val db=this.writableDatabase

        db?.execSQL(createTblReviewsProfile)

        val success= db.delete(TBL_REVIEWS_PROFILE, "REVIEW_ID>0", null)
        db.close()

        return success
    }

    fun truncateTableReviewsImage(reviewImg:Int): Int{

        createReviewsImage()
        val db=this.writableDatabase

        val success= db.delete(TBL_REVIEWS_IMGS, "REVIEW_ID==$reviewImg", null)
        db.close()

        return success
    }

    fun createReviewsImage(){

        //la tabla de reviews que se muestra en el perfil
        val createTblReviewsImg= ("CREATE TABLE IF NOT EXISTS " + TBL_REVIEWS_IMGS+ "("+
                REVIEW_IMG_ID+" BIGINT PRIMARY KEY,"+
                REVIEW_ID+" INT, "+
                IMG_PATH+" TEXT)")

        val db=this.writableDatabase

        db?.execSQL(createTblReviewsImg)
        db.close()
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getAllReviewsProfile(): ArrayList<ReviewProfileModel>{
        val stdList: ArrayList<ReviewProfileModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_REVIEWS_PROFILE"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var title:String
        var description:String
        var images:String
        var user_id:Int
        var nickname:String
        var restaurant_id:Int
        var name:String

        if(cursor.moveToFirst())
        {
            do{

                id=cursor.getInt(cursor.getColumnIndex("review_id"))
                title=cursor.getString(cursor.getColumnIndex("title"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                images=cursor.getString(cursor.getColumnIndex("image"))
                user_id=cursor.getInt(cursor.getColumnIndex("user_id"))
                nickname=cursor.getString(cursor.getColumnIndex("alias"))
                restaurant_id=cursor.getInt(cursor.getColumnIndex("restaurant_id"))
                name=cursor.getString(cursor.getColumnIndex("name"))

                val std = ReviewProfileModel(id, title, description, images, user_id, nickname, restaurant_id, name)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    /*Insertar restaurantes*/
    fun insertRestaurantsProfile(std: RestaurantModel):Long{
        val db=this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(RESTAURANT_ID, std.id)
        contentValues.put(NAME, std.name)
        contentValues.put(DESCRIPTION, std.description)
        contentValues.put(IMG, std.image)

        val success=db.insert(TBL_RESTAURANTS_PROFILE, null, contentValues)
        db.close()
        return success
    }

    fun truncateTableRestaurantsProfile(): Int{
        //la tabla de reviews que se muestra en el perfil
        val createTblRestaurantProfile= ("CREATE TABLE IF NOT EXISTS " + TBL_RESTAURANTS_PROFILE+ "("+
                RESTAURANT_ID+" BIGINT PRIMARY KEY,"+
                NAME+ " TEXT," +
                DESCRIPTION + " TEXT, "+
                IMG+" TEXT)")

        val db=this.writableDatabase

        db?.execSQL(createTblRestaurantProfile)

        val success= db.delete(TBL_RESTAURANTS_PROFILE, "restaurant_id>0", null)
        db.close()

        return success
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getAllRestaurantsProfile(): ArrayList<RestaurantModel>{
        val stdList: ArrayList<RestaurantModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_RESTAURANTS_PROFILE"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var name: String
        var description: String
        var image: String

        if(cursor.moveToFirst())
        {
            do{
                id=cursor.getInt(cursor.getColumnIndex("restaurant_id"))
                name=cursor.getString(cursor.getColumnIndex("name"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                image=cursor.getString(cursor.getColumnIndex("image"))

                val std = RestaurantModel(id, name, description, image)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    fun insertReviewsHome(std: ReviewProfileModel):Long{
        val db= this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(REVIEW_ID, std.id)
        contentValues.put(TITLE, std.title)
        contentValues.put(DESCRIPTION, std.description)
        contentValues.put(IMG, std.images)
        contentValues.put(USER_ID, std.user_id)
        contentValues.put(ALIAS, std.user_nickname)
        contentValues.put(RESTAURANT_ID, std.restaurant_id)
        contentValues.put(NAME, std.restaurant_name)

        val success=db.insert(TBL_REVIEWS_HOME, null, contentValues)
        db.close()
        return success
    }

    fun truncateTableReviewsHome(): Int{

        //la tabla de reviews que se muestra en el perfil
        val createTblReviewsProfile= ("CREATE TABLE IF NOT EXISTS " + TBL_REVIEWS_HOME+ "("+
                REVIEW_ID+" BIGINT PRIMARY KEY,"+
                TITLE+ " TEXT," +
                DESCRIPTION + " TEXT, "+
                IMG+" TEXT,"+
                USER_ID+" BIGINT,"+
                ALIAS +" TEXT, "+
                RESTAURANT_ID + " BIGINT,"+
                NAME+" TEXT)")

        val db=this.writableDatabase

        db?.execSQL(createTblReviewsProfile)

        val success= db.delete(TBL_REVIEWS_HOME, "REVIEW_ID>0", null)
        db.close()

        return success
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getAllReviewsHome(): ArrayList<ReviewProfileModel>{
        val stdList: ArrayList<ReviewProfileModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_REVIEWS_HOME"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var title:String
        var description:String
        var images:String
        var user_id:Int
        var nickname:String
        var restaurant_id:Int
        var name:String

        if(cursor.moveToFirst())
        {
            do{

                id=cursor.getInt(cursor.getColumnIndex("review_id"))
                title=cursor.getString(cursor.getColumnIndex("title"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                images=cursor.getString(cursor.getColumnIndex("image"))
                user_id=cursor.getInt(cursor.getColumnIndex("user_id"))
                nickname=cursor.getString(cursor.getColumnIndex("alias"))
                restaurant_id=cursor.getInt(cursor.getColumnIndex("restaurant_id"))
                name=cursor.getString(cursor.getColumnIndex("name"))

                val std = ReviewProfileModel(id, title, description, images, user_id, nickname, restaurant_id, name)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    fun insertReviewsBorrador(std: ReviewModel):Long{
        val db= this.writableDatabase

        val contentValues= ContentValues()
        //contentValues.put(REVIEW_ID, std.id)
        contentValues.put(TITLE, std.title)
        contentValues.put(DESCRIPTION, std.description)
        contentValues.put(USER_ID, std.user_id)
        contentValues.put(RESTAURANT_ID, std.restaurant_id)

        val success=db.insert(TBL_REVIEWS_BORRADOR, null, contentValues)
        db.close()
        return success
    }

    fun createTableBorrador(){

        //la tabla de reviews que se muestra en el perfil
        val createTblReview= ("CREATE TABLE IF NOT EXISTS " + TBL_REVIEWS_BORRADOR+ "("+
                REVIEW_ID+" BIGINT PRIMARY KEY,"+
                TITLE+ " TEXT," +
                DESCRIPTION + " TEXT, "+
                USER_ID+" BIGINT,"+
                RESTAURANT_ID+" BIGINT)")

        val db=this.writableDatabase

        db?.execSQL(createTblReview)

    }

    fun truncateTableReviewsBorrador(): Int{

        //la tabla de reviews que se muestra en el perfil
        createTableBorrador()

        val db=this.writableDatabase
        val success= db.delete(TBL_REVIEWS_BORRADOR, null, null)
        db.close()

        return success
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getAllReviewsBorrador(): ArrayList<ReviewModel>{
        val stdList: ArrayList<ReviewModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_REVIEWS_BORRADOR"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id:Int
        var title:String
        var description:String
        var user_id:Int
        var restaurant_id:Int

        if(cursor.moveToFirst())
        {
            do{

                id=cursor.getInt(cursor.getColumnIndex("review_id"))
                title=cursor.getString(cursor.getColumnIndex("title"))
                description=cursor.getString(cursor.getColumnIndex("description"))
                user_id=cursor.getInt(cursor.getColumnIndex("user_id"))
                restaurant_id=cursor.getInt(cursor.getColumnIndex("restaurant_id"))

                val std = ReviewModel(id, title, description, user_id, restaurant_id)
                stdList.add(std)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    fun createTableReviewImages(){

        //la tabla de reviews que se muestra en el perfil
        val createTblReview= ("CREATE TABLE IF NOT EXISTS " + TBL_REVIEW_IMAGES_BORR+ "("+
                REVIEW_IMG_ID+" BIGINT PRIMARY KEY,"+
                IMG_PATH+ " TEXT," +
                REVIEW_ID+" BIGINT)")

        val db=this.writableDatabase

        db?.execSQL(createTblReview)
        db.close()
    }

    fun truncateTableReviewImages(): Int{

        val db=this.writableDatabase
        val success= db.delete(TBL_REVIEW_IMAGES_BORR, null, null)
        db.close()

        return success
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getReviewImagesId(review_id: Int): ArrayList<String>{
        val stdList: ArrayList<String> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_REVIEW_IMAGES_BORR WHERE $REVIEW_ID= $review_id"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        //var id:Int
        var image_path:String
        //var review_id:Int

        if(cursor.moveToFirst())
        {
            do{

                //id=cursor.getInt(cursor.getColumnIndex("review_image_id"))
                image_path=cursor.getString(cursor.getColumnIndex("image_path"))
                //review_id=cursor.getInt(cursor.getColumnIndex("review_id"))

                //val std = ReviewImagesModel(id, image_path, review_id)
                //stdList.add(std)
                stdList.add(image_path)
            } while (cursor.moveToNext())
        }
        return stdList
    }

    /*Obtener a todos los estudiantes y retornar una lista con los resultados del query*/
    fun getReviewImagesIdDetail(review_id: Int): ArrayList<String>{
        val stdList: ArrayList<String> = ArrayList()
        val selectQuery= "SELECT * FROM $TBL_REVIEWS_IMGS WHERE $REVIEW_ID= $review_id"
        val db= this.readableDatabase

        val cursor: Cursor?
        try{
            cursor=db.rawQuery(selectQuery, null)
        }catch(e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        //var id:Int
        var image_path:String
        //var review_id:Int

        if(cursor.moveToFirst())
        {
            do{

                //id=cursor.getInt(cursor.getColumnIndex("review_image_id"))
                image_path=cursor.getString(cursor.getColumnIndex("image_path"))
                //review_id=cursor.getInt(cursor.getColumnIndex("review_id"))

                //val std = ReviewImagesModel(id, image_path, review_id)
                //stdList.add(std)
                stdList.add(image_path)
            } while (cursor.moveToNext())
        }
        return stdList
    }


    fun insertReviewsImgsBorr(std: ReviewImagesModel):Long{
        val db= this.writableDatabase

        val contentValues= ContentValues()
        //contentValues.put(REVIEW_IMG_ID, std.review_image_id)
        contentValues.put(IMG_PATH, std.image_path)
        contentValues.put(REVIEW_ID, std.review_id)

        val success=db.insert(TBL_REVIEW_IMAGES_BORR, null, contentValues)
        db.close()
        return success
    }

    fun insertReviewsImgs(std: ReviewImagesModel):Long{
        createReviewsImage()
        val db= this.writableDatabase

        val contentValues= ContentValues()
        //contentValues.put(REVIEW_IMG_ID, std.review_image_id)
        contentValues.put(IMG_PATH, std.image_path)
        contentValues.put(REVIEW_ID, std.review_id)

        val success=db.insert(TBL_REVIEWS_IMGS, null, contentValues)
        db.close()
        return success
    }

}