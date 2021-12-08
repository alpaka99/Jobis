package com.ssafy.jobis.data.model.calendar

import android.content.Context
import androidx.room.*

@Database(entities = [RoutineSchedule::class], version = 1)
@TypeConverters(Converters::class)
abstract class RoutineScheduleDatabase: RoomDatabase() {
    abstract fun routineScheduleDao(): RoutineScheduleDao

    companion object {
        private var instance: RoutineScheduleDatabase? = null

        @Synchronized
        fun getInstance(context: Context?): RoutineScheduleDatabase?{
            if (instance == null) {
                synchronized(RoutineScheduleDatabase::class) {
                    instance = Room.databaseBuilder(
                        context!!.applicationContext,
                        RoutineScheduleDatabase::class.java,
                        "routine-schedule-database"
                    ).build()
                }
            }
            return instance
        }
    }
}