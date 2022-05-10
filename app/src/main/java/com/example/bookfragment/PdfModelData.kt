package com.example.bookfragment

class PdfModelData{
    //variables
    var uid:String =""
    var id:String =""
    var title:String =""
    var description:String =""
    var categoryId:String =""
    var url:String = ""
    var timestamp:Long = 0
    var viewsCount:Long=0
    var downloadsCount:Long = 0
    var isReserved = false
    // empty constructor (required by firebase)
    constructor()
    //parameterized constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCount: Long,
        isReserved : Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
        this.isReserved = isReserved
    }


}