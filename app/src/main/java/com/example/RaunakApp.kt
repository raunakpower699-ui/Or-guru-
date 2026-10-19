package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.NumberRepository
import com.example.data.worker.DailyRefreshWorker

class RaunakApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: NumberRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = NumberRepository(database)
        DailyRefreshWorker.scheduleDailyWork(this)
    }
}
