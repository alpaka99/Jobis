# Material CalendarView DotSpan



CalendarFragment에서 달력에 Dot을 찍어줄 때, Coroutine을 사용하였다.

```kotlin
fun dotDecorator(calendar: MaterialCalendarView?, scheduleDatabase: CalendarDatabase?, routineScheduleDatabase: RoutineScheduleDatabase?) {
        // 사전작업1. room에서 단일 일정 데이터 가져와서 표시해주기
        var dates = ArrayList<CalendarDay>()

        CoroutineScope(Dispatchers.IO).launch {
            var scheduleList = scheduleDatabase!!.calendarDao().getAll()
            if (scheduleList.size != 0) { // schedule이 있을 때만..
                for (i in 0..scheduleList.size-1) {
                    var dot_year = scheduleList[i].year
                    var dot_month = scheduleList[i].month
                    var dot_day = scheduleList[i].day
                    println("DB결과: " + scheduleList[i])
                    // 점 찍기 => 여러 날에 표시하려고 days를 구성해서 추가해주는 방식..
                    // 달력에 표시할 날짜 가져오기
                    var date = Calendar.getInstance()
                    date.set(dot_year, dot_month, dot_day)

                    // 달력에 표시할 날짜 day List에 넣기
                    var day = CalendarDay.from(date) // Calendar 자료형을 넣어주면 됨
                    dates.add(day)

                    // 글자 표시는 하루하루 해줘야 함
                    var date_for_text = ArrayList<CalendarDay>()
                    date_for_text.add(day)
                    calendar!!.addDecorator(TextDecorator(date_for_text, scheduleList[i].title))
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            // 점은 처음부터 다시 찍어야 함
            calendar!!.removeDecorators()
            calendar!!.invalidateDecorators()
            // 토, 일 색칠 + 오늘 날짜 표시
            calendar.addDecorators(SundayDecorator(), SaturdayDecorator(), OneDayDecorator())
            if (dates.size > 0) {
                calendar!!.addDecorator(EventDecorator(Color.parseColor("#3f51b5"), dates)) // 점 찍기
            }
            println("dates size:" + dates)
        }, 0)


        // 사전작업2. room에서 반복 일정 데이터 가져와서 표시해주기
        var routineDates = ArrayList<ArrayList<CalendarDay>>()
        CoroutineScope(Dispatchers.IO).launch {
            var routineScheduleList = routineScheduleDatabase!!.routineScheduleDao().getAll()
            for (i: Int in 0..routineScheduleList.size-1) {
                var dates = ArrayList<CalendarDay>() // 점을 찍을 날짜들을 여기에 add로 담아주면 됨
                // 하나의 반복 일정에 대해
                for (j: Int in 0..routineScheduleList[i].dayList!!.size-1) {
                    var routine = routineScheduleList[i].dayList!![j] // 날짜
                    val routine_year = routine.get(Calendar.YEAR)
                    val routine_month = routine.get(Calendar.MONTH)
                    val routine_day = routine.get(Calendar.DAY_OF_MONTH)
                    var date = Calendar.getInstance()
                    date.set(routine_year, routine_month, routine_day)

                    var day = CalendarDay.from(date) // Calendar 자료형을 넣어주면 됨
                    dates.add(day)
                }
                routineDates.add(dates)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            calendar!!.removeDecorators()
            calendar!!.invalidateDecorators()
            calendar.addDecorators(SundayDecorator(), SaturdayDecorator(), OneDayDecorator())
            for (v: Int in 0..routineDates.size-1) {
                calendar!!.addDecorator(EventDecorator(Color.parseColor("#3f51b5"), routineDates[v])) // 점 찍기
            }
            println("dates2 size:" + routineDates)
        }, 0)
    }
```

위 코드는 완성된 코드로, 점을 찍은 후 일정을 제거할 때 점이 같이 삭제되도록 하기 위해 많은 시간을 소모했다.



## 문제점

1. scheduleList는 DB에서 가져오는데, MainThread에서 이런 무거운 동작을 하게 되면 ANR(Activity Not Response)이 발생할 수 있다고 한다. 그래서 애초에  다음과 같이 coroutine을 사용해야 DB에서 정보를 가져올 수 있었다.

   ```kotlin
   CoroutineScope(Dispatchers.IO).launch {
               var scheduleList = scheduleDatabase!!.calendarDao().getAll()
   }
   ```

   ANR은 어떤 연산에 5초 이상 시간이 걸려 그동안 사용자 이벤트에 반응하지 못하게 되면 발생하는 에러로, close app이나 wait의 옵션이 뜨는 alert 창이 보이게 된다.

2. 이렇게 Main이 아니라 다른 thread에서 비동기 작업을 수행하여 가져온 scheduleList를 활용하여 dates 변수에 각 날짜를 담아야 decorator에서 사용할 수 있었기 때문에 main이 아닌 coroutine으로 만든 thread에서 dates를 만들고, decorator까지 사용하였다.

3. 이렇게 맨 처음 decorator를 추가하는데에는 문제가 없었지만, 스케줄이 삭제되어 decorator를 지워야 할 때 문제가 발생했다. Main thread가 아니면 UI를 수정할 수 없기 때문에 발생한 문제로, addDecorator 부분을 coroutine 바깥으로 빼주어야 했다.

   => 여기서 원래는 `CoroutineScope(Dispatchers.MAIN)`으로 변경하면 되지만, DB로부터 정보를 가져와야 해서 MAIN은 사용 불가하다.

4. addDecorator를 coroutine 바깥으로 빼주면, coroutine 내부에서 DB로부터 불러와 선언한 scheduleList를 사용할 수 없고, 따라서 coroutine 내부에서 scheduleList를 사용하여 작업을 한 후, 그 결과를 바깥에서 이용할 수 있어야 했다.

5. 그래서 dates 변수를 coroutine 밖으로 빼냈고(원래 안에 있었음), 문제가 해결될 것 같았다.

6. 그러나 coroutine은 비동기 작업이기 때문에 결과가 바로 dates에 담기지 않는 문제가 있었고, 야매스럽지만 웹을 만들때도 유용하게 썼던 js의 setTimeOut 처럼 delay를 주기로 했다.

7. delay 주는 방법을 찾아보니 handler를 쓰면 된다고 해서 썼음!

   ```kotlin
   Handler(Looper.getMainLooper()).postDelayed({
               // 점은 처음부터 다시 찍어야 함
               calendar!!.removeDecorators()
               calendar!!.invalidateDecorators()
               // 토, 일 색칠 + 오늘 날짜 표시
               calendar.addDecorators(SundayDecorator(), SaturdayDecorator(), OneDayDecorator())
               if (dates.size > 0) {
                   calendar!!.addDecorator(EventDecorator(Color.parseColor("#3f51b5"), dates)) // 점 찍기
               }
               println("dates size:" + dates)
           }, 0)
   ```

   decorator를 전부 지워주고, 토, 일 색칠 + 오늘 날짜 강조표시 한 후 점 찍을 날짜에 찍어주는 순서이고, delay를 0으로 줘서 coroutine이 끝나면 즉시 실행되도록 하였다.

   (웹에서 settimeout을 주면 우선순위가 뒤로 밀리는 것을 생각하고 써봤는데 똑같았다.)

