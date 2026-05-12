package com.example.codexiabeta.data.repository

import com.example.codexiabeta.data.dao.ShelfDao
import com.example.codexiabeta.data.entity.ShelfEntity
import kotlinx.coroutines.flow.Flow

class ShelfRepository(private val shelfDao: ShelfDao) {

    fun getAllShelves(): Flow<List<ShelfEntity>> = shelfDao.getAllShelves()

    suspend fun getShelfByName(name: String): ShelfEntity? = shelfDao.getShelfByName(name)

    suspend fun insertShelf(shelf: ShelfEntity) = shelfDao.insertShelf(shelf)
}
