package com.ssafy.jobis.data.model.study

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Study::class, Crew::class, Chat::class], version = 1)
@TypeConverters(Converters::class)
abstract class StudyDatabase : RoomDatabase() {

    abstract fun getStudyDao(): StudyDao

    companion object {
        private var Instance: StudyDatabase? = null

        fun getInstance(context: Context): StudyDatabase? {
            if(Instance == null) {
                synchronized(StudyDatabase::class) {
                    Instance = Room.databaseBuilder(
                        context,
                        StudyDatabase::class.java,
                        "study"
                    ).build()
                }
            }
            return Instance
        }

    }
}