package com.ssafy.jobis.data.model.calendar

import androidx.room.*

@Dao
interface RoutineScheduleDao {
    @Query("SELECT * FROM routineSchedule")
    fun getAll(): List<RoutineSchedule>

    @Query("DELETE FROM RoutineSchedule where id = :id")
    fun deleteRoutineSchedules(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routineSchedule: RoutineSchedule) : Long

    @Update
    fun update(routineSchedule: RoutineSchedule)

    @Delete
    fun delete(routineSchedule: RoutineSchedule)

}
