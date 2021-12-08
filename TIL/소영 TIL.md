# 소영 TIL



### 1. local.properties 파일

```
SDK location not found. Define location with an ANDROID_SDK_ROOT environment variable or by setting the sdk.dir path in your project's local properties file at '~/android/local.properties'.
```

 run 했을 때 상단의 오류 발생.

- local.properties? 프로젝트 생성 시 자동 생성되는 파일. 안드로이드 SDK의 경로가 지정되어 있다.

  참고:  http://sunphiz.me/wp/archives/1828

- 없으면 생성해주면 된다길래 그냥 하나 만들었더니 정상 작동

- .gitinore 파일에 들어가있음! 자기 컴퓨터 경로에 맞게 하나 만들면 되는듯

### 2. 시간 데이터 타입

전부다 string으로 바꿔서 씀 (데이터 타입이 따로 있는 것 같긴 한데 못찾음) 나중에 찾아볼 예정

### 3. Room 

```
Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number.
```

기존 데이터가 남아서 그렇다 

해결책을 찾아보다가 그냥 Virtual Deviece를 하나 추가해서 다른걸 이용하는거로 해결 (임시)

데이터 삭제하는 방법 찾아야 함

### 4. 특정 기간 동안의 반복 일정 

1. 시작 날짜와 끝 날짜 `DAY_OF_YEAR` 포맷으로 받아온다.

   ```kotlin
   // 시작 날짜와 끝 날짜는 미리 세팅해둔다
   startCal.set(startYear, startMonth, startDay)
   // 시작 날짜와 끝 날자를 DAY OF YEAR 로 받아온다
   var startDayOfYear = startCal.get(Calendar.DAY_OF_YEAR)
   var endDayOfYear = endCal.get(Calendar.DAY_OF_YEAR)
   ```

2. 시작 날짜의 요일 확인

   ```kotlin
    var startDayOfWeek= startCal.get(Calendar.DAY_OF_WEEK)
   ```

3. 반복 일정으로 선택한 요일을 확인, 시작 날짜의 요일과 비교해 연산 및 추가

   ```kotlin
   // routineScheduleFragment.kt
   // 선택할 날짜를 담을 Calendar객체. 이 상태에서는 현재 날짜 시간이 담겨있다 
   var addCal = Calendar.getInstance()
   // dayofWeekSelect = 반복 선택 요일 
   for(i in dayofWeekSelect) {            
       if (i == startDayOfWeek) { // 요일이 같다면
           var addDay = startDayOfYear // 시작 날짜 변수에 저장 
           while(addDay <= endDayOfYear){ // 끝 날짜보다 작을 때 반복
               // DAY_OF_YEAR(addDay) 포맷으로 addCal(Calendar) 객체에 저장
               addCal.set(Calendar.DAY_OF_YEAR, addDay) 
               // 이후 빈칸에 원하는 포맷으로 저장
               routineDaySelect.add(빈칸)
               // 포맷 설정 예시
               // addCal.get(Calendar.DAY_OF_MONTH)
               // +7 (주별 루틴)
               addDay += 7
           }
       } else if (i > startDayOfWeek) { // 시작 날짜 요일이 선택 요일보다 클때
           var tmp = i - startDayOfWeek
           var addDay = startDayOfYear + tmp
           while(addDay <= endDayOfYear){
               addCal.set(Calendar.DAY_OF_YEAR, addDay)
               println("addCal, $addCal")
               routineDaySelect.add(addDay)
               addDay += 7
           }
       } else { // 시작 날짜 요일이 선택 요일보다 작을때
           var tmp = 7 - startDayOfWeek + i
           var addDay = startDayOfYear + tmp
           while(addDay <= endDayOfYear){
               routineDaySelect.add(addDay)
               addDay += 7
           }
       }
   }
   ```

### 5. 새 기기 등록시 뜨는 에러

```kotlin
java.lang.AssertionError: Method getAlpnSelectedProtocol not supported for object SSL socket over Socket[address=firestore.googleapis.com/34.64.4.10,port=443,localPort=38172]

```

- ???

### 6. Array 한 줄 출력

```kotlin
 arrayName = arrayOf(1, 2, 3)
 println(arrayName.contentToString()) // [1, 2, 3]
```

mutableListOf 으로 만든건 그냥 출력해도 나온다

arrayof는 안됨

### 7. 버튼 클릭했을 때 모양 변경

```kotlin
// drawble에 추가 > selected 이용
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_selected="true"
        android:drawable="@drawable/schedule_day_of_week_btn_true"/>
    <item android:drawable="@drawable/schedule_day_of_week_btn_false"/>
</selector>
```

```kotlin
// fragment.xml
// 클릭시 isSelected 상태 변환
view.dayOfWeek1.setOnClickListener {
    dayOfWeek1.isSelected = !dayOfWeek1.isSelected
}
```



