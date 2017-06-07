# OpenCT-MVP


School Schedule and Library Application, implementing MVP Design Pattern, and with open source


This is an Open Source Project, which aims to be the start point for your development of your own school. fork it, and start your own project now!

see it on coolapk - [http://www.coolapk.com/apk/cc.metapro.openct](http://www.coolapk.com/apk/cc.metapro.openct)

## Features

1. Fetch your classes, update current week automatically
2. Fetch your academic grade
3. Search library
4. Fetch books you have borrowed from library
5. Export to iCal file (.ics) for Calendar Sync
6. Add & edit classes manually
7. Import class from Excel table content
8. Query your CET grade
9. Theme selection
10. App widget
11. Script based school custom (Coming soon!)

## Dependency

- Jsoup
- Google Gson
- Android Support v4/v7/Design/CardView-v7
- ReactiveX RxJava/RxAndroid
- Squareup Retrofit/Converter-Gson/Converter-Scalars/Adapter-RxJava2
- Mnode iCal4j
- Jakewharton ButterKnife
- jp.wasabeef:recyclerview-animators
- com.github.clans:fab
- se.emilsjolander:stickylistheaders
- com.rengwuxian.materialedittext:library
- org.xdty.preference:color-picker
- com.yanzhenjie:recyclerview-swipe
- com.wdullaer:materialdatetimepicker
- com.github.clans:fab
- com.scottyab:aescrypt

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
