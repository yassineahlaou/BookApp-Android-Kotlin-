package com.example.myproject

import java.sql.Timestamp

class ModelComment {

    //variables, shouta be with sane spetings and type as we added in firebase
    var id = ""
    var bookId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    //empty constructor, required by firebase
    constructor()

    //param construetor
    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }
}