# OpenCT-MVP


开源图书馆与课程表应用


## ScreenShots

<img src="./screenshots/features.jpg?raw=true" width="200">
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


## 功能


获取教务网课程表与成绩信息, 自动更新周数 (目前支持的教务系统有 `正方, 苏文`)


搜索图书馆馆藏信息, 获取个人图书借阅信息 (目前支持 `汇文OPACv4.5+`)


## 特性


自动解析并填写表单 (包括 搜索表单, 用户登录表单)


支持 `不使用Js做加密处理的` `用户名 - 密码 ( - 验证码)` 的登录表单结构


(使用Js作加密处理的后续版本会提供解决)

## 开发进度


1. 正在向 Dagger 迁移
2. 正在开发 空教室查询
3. 服务端正在开发中(提供空教室查询服务)


## 自定义功能地址配置示例


如 `武汉工程大学`


在浏览器地址栏的显示为 `http://218.199.178.12/(fath5dejk2robp45sfgikn45)/` 需要勾选 动态地址


而在地址一栏填写的是 `http://218.199.178.12/`
