# OpenCT-MVP


开源图书馆与课程表应用


## 软件截图


<img src="./screenshots/features.JPG?raw=true" width="200">
<img src="./screenshots/class_table.JPG?raw=true" width="200">
<img src="./screenshots/borrow_info.JPG?raw=true" width="200">
<img src="./screenshots/search_result.JPG?raw=true" width="200">


## 依赖


- Jsoup 1.10.1
- android support v4, v7, design, cardview-v7
- Gson
- Guava, (for null check)
- Retrofit with Converter-gson and Converter-scalars
- Recyclerview-Animators
- ButterKnife
- RxJava 2.0.3
- RxAndroid 2.0.1


## 功能


获取教务网课程表与成绩信息, 自动更新周数 (目前支持的教务系统有 `正方, 苏文`)


搜索图书馆馆藏信息, 获取个人图书借阅信息 (目前支持 `汇文OPACv4.5+`)


## 特性


自动解析登录表单 (理论上所有`用户名-密码(-验证码)`的登录表单结构均可自动解析)


软件内教务系统以及图书馆均为自动解析登录


自动解析图书搜索表单 (汇文软件OPACv4.5+测试通过)


用户名密码本地加密存储


## 开发进度


正在向 Dagger 迁移


正在开发 `空教室查询功能`


服务端正在开发中(提供空教室查询服务)

## 自定义功能配置示例


如 `武汉工程大学`


在浏览器地址栏的显示为 `http://218.199.178.12/(fath5dejk2robp45sfgikn45)/` 需要勾选 动态地址


而在地址一栏填写的是 `http://218.199.178.12/`
