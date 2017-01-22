# OpenCT-MVP


开源图书馆与课程表应用


## ScreenShots


<img src="./screenshots/class_table_day.jpg?raw=true" width="200">
<img src="./screenshots/class_table_week.jpg?raw=true" width="200">
<img src="./screenshots/borrow_info.jpg?raw=true" width="200">
<img src="./screenshots/search_result.jpg?raw=true" width="200">
<img src="./screenshots/custom.jpg?raw=true" width="200">


## Dependency


- Jsoup 1.10.1
- Google Gson 2.7, Guava 20.0 // Dagger 2.8 (not used now)
- Android Support v4/v7/Design/CardView-v7 25.1
- ReactiveX RxJava 2.0.3, RxAndroid 2.0.1
- Squareup Retrofit 2.1.0, Converter-Gson 2.1.0, Converter-Scalars 2.1.0
- Jakewharton ButterKnife 8.4.0
- Mnode iCal4j 2.0.0
- jp.wasabeef:recyclerview-animators 2.2.4
- com.github.clans:fab 1.6.4
- se.emilsjolander:stickylistheaders 2.7.0
- com.rengwuxian.materialedittext:library 2.1.4



## 功能


1. 获取教务网课程表, 自动更新周数
2. 获取教务网成绩信息
3. 搜索图书馆馆藏信息
4. 获取个人图书借阅信息
5. 导出课表到日历
6. 从 Excel 文件导入课表(正在开发)
7. 增加更多课程信息(正在开发)


## 特性


自动解析并填写表单 (包括 搜索表单, 用户登录表单)


## 开发进度


0. 正在开发完善 高级自定义功能
1. 正在逐步实现 `正方` 课表, 成绩完整查询
2. 正在逐步实现 `强智` 课表, 成绩完整查询
3. 正在逐步实现 `青果` 课表, 成绩完整查询
4. 项目正在向 Dagger 迁移
5. 正在开发 空教室查询
