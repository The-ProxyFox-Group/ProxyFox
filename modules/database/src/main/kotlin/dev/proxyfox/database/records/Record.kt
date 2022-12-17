package dev.proxyfox.database.records

interface Record {
    fun toMongo(): MongoRecord
}
