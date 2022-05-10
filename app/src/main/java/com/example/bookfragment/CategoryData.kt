package com.example.bookfragment

class CategoryData{
    var id:String =""
    var category:String = ""
    var timestamp: Long = 0
    var uid:String = ""
    //empty constracor required by database
    constructor()
    constructor(

        id: String,

        category: String,

        timestamp: Long,
        uid :  String
    ) {

        this.id = id

        this.category = category

        this.timestamp = timestamp
        this.uid = uid

    }
}
