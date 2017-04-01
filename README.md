# OpenCT-MVP


School Schedule and Library Application, implementing MVP Design Pattern, and with open source


This is an Open Source Project, which aims to be the start point for your development of your own school. fork it, and start your own project now!


## ScreenShots

<img src="./screenshots/features.jpg?raw=true" width="100">
<img src="./screenshots/class_table_day.jpg?raw=true" width="100">
<img src="./screenshots/class_table_week.jpg?raw=true" width="100">
<img src="./screenshots/borrow_info.jpg?raw=true" width="100">
<img src="./screenshots/search_result.jpg?raw=true" width="100">
<img src="./screenshots/all_classes.jpg?raw=true" width="100">
<img src="./screenshots/class_edit.jpg?raw=true" width="100">
<img src="./screenshots/view_in_icalendar.png?raw=true" width="100">

## Dependency

- Jsoup 1.10.2
- Google Gson 2.8.0
- Android Support v4/v7/Design/CardView-v7 25.1
- ReactiveX RxJava 2.0.5/RxAndroid 2.0.1
- Squareup Retrofit 2.1.0/Converter-Gson 2.1.0/Converter-Scalars 2.1.0
- Mnode iCal4j 2.0.0
- Jakewharton ButterKnife 8.5.1
- jp.wasabeef:recyclerview-animators 2.2.4
- com.github.clans:fab 1.6.4
- se.emilsjolander:stickylistheaders 2.7.0
- com.rengwuxian.materialedittext:library 2.1.4
- org.xdty.preference:color-picker 0.0.4
- com.yanzhenjie:recyclerview-swipe 1.0.2
- com.wdullaer:materialdatetimepicker 3.1.2
- com.github.clans:fab 1.6.4
- com.scottyab:aescrypt 0.0.1
- me.relex:circleindicator 1.2.2@aar

## Features


1. Fetch your classes, update current week automatically
2. Fetch your academic grade
3. Search library
4. Fetch book info you have borrowed
5. Export to iCal file (.ics) for Calendar Sync
6. Add & edit classes manually
7. Import class from Excel table content
8. Query your CET grade


## Import from XLSX (Excel 2007+)

Download Excel Template Document - <a href="./template.xlsx?raw=true">template.xlsx</a>

|key|description|
|----|----|
|name|The ClassName (e.g. 大学语文)|
|type|The ClassType (e.g. 必修)|
|time|The Time you should attend class, use Chinese for weekday and leave only one time here per line (e.g. 周一5-6)|
|during|The Duration of the class, support odd, even week (use Chinese Please) and multiple duration, use & to separate them (e.g. 1-5&6-10双&11-15单)|
|teacher|The teacher name who lecture the class (e.g. 老王)|
|place|The place to attend the class (e.g. 教102)|


### Example Table Content


|name|type|time|during|teacher|place|
|----|----|----|-----|----|----|
|大学语文|必修|周一 5-6节|1-5&6-10双&11-15单|老王|教102|
|大学语文|必修|周三 1-2节|1-5周|老李|教103|
|Android开发|选修|周日 1-5节|1-18周|自己|图书馆|
