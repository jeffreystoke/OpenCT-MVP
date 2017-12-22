# OpenCT-Android

开源课程表 (Android 版) 使用 Kotlin 编写, 基于 Room, MVVM 完全重新设计构建

酷安市场 [开源课程表](http://www.coolapk.com/apk/cc.metapro.openct)

## Features

1. 获取并显示课程
1. 获取并显示成绩 (可查英语四六级)
1. 搜索图书馆
1. 获取图书馆借阅信息
1. JS 自定义脚本登录教务网
1. 导出 iCal 文件 (.ics) 用于日历同步
1. 手动添加修改课程信息
1. Excel 文件导入课程信息
1. 主题切换

## Dependency

- openct-core
- RxJava2 & RxAndroid
- Android Support v4/v7/Design/CardView-v7/ConstraintLayout

## Import from XLSX (Excel 2007+)

Download Excel Template Document - <a href="./template.xlsx?raw=true">template.xlsx</a>

|列名|描述|
|----|----|
|name|课程名称 (e.g. 大学语文)|
|type|课程类型 (e.g. 必修)|
|time|上课时间, 用中文表示周几, 一行只能有一个时间 (e.g. 周一5-6)|
|during|上课周期, 用中文表示单双周, 有多个周期时使用 `&` 分隔 (e.g. 1-5&6-10双&11-15单)|
|teacher|上课教师 (e.g. 老王)|
|place|上课地点 (e.g. 教102)|

### Example Table Content

|name|type|time|during|teacher|place|
|----|----|----|-----|----|----|
|哲学|必修|周一 5-6节|1-5&6-10双&11-15单|波澜哥|教102|
