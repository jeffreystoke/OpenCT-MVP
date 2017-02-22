# OpenCT-MVP


开源课程表与图书馆应用


目前这只是一个参考项目, 欢迎添加分支自定义自己的学校


## ScreenShots


<img src="./screenshots/features.jpg?raw=true" width="200">
<img src="./screenshots/class_table_day.jpg?raw=true" width="200">
<img src="./screenshots/class_table_week.jpg?raw=true" width="200">
<img src="./screenshots/borrow_info.jpg?raw=true" width="200">
<img src="./screenshots/search_result.jpg?raw=true" width="200">
<img src="./screenshots/custom.jpg?raw=true" width="200">


## Dependency


- Jsoup 1.10.2
- Google Gson 2.8.0
- Android Support v4/v7/Design/CardView-v7 25.1
- ReactiveX RxJava 2.0.5, RxAndroid 2.0.1
- Squareup Retrofit 2.1.0, Converter-Gson 2.1.0, Converter-Scalars 2.1.0
- Mnode iCal4j 2.0.0
- Jakewharton ButterKnife 8.5.1
- jp.wasabeef:recyclerview-animators 2.2.4
- com.github.clans:fab 1.6.4
- se.emilsjolander:stickylistheaders 2.7.0
- com.rengwuxian.materialedittext:library 2.1.4
- org.xdty.preference:color-picker 0.0.4
- com.yanzhenjie:recyclerview-swipe 1.0.2



## 功能


1. 获取教务网课程表, 自动更新周数
2. 获取教务网成绩信息
3. 搜索图书馆馆藏信息
4. 获取个人图书借阅信息
5. 导出课表到日历
6. 增加/修改 课程信息
7. 从 Excel 文件导入课表(正在开发)


## 开发进度


0. 正在开发完善 高级自定义功能
1. 正在逐步实现 `正方` 课表, 成绩完整查询 (已实现)
2. 正在逐步实现 `强智` 课表, 成绩完整查询 (已实现)


## 课程导入表格字段

name - 对应 课程名称 (如 大学语文)
type - 对应 课程类型 (如 必修)
time - 对应 上课时间, 请确保只有一个时间 (如 周一 5-6节)
during - 对应 课程周期, 支持单双周多字段, 多个周期请用 & 分隔 (如 1-5&6-10双&11-15单)
teacher - 对应 授课教师 (如 老王)
place - 对应 上课地点 (如 人民广场)

若一节课有多个上课时间, 需要将其作为多个表项添加, 不能在 time 字段添加多个时间

填写完成后将内容复制到Excel导出对话框中的输入框中, 点击确定即可生成

模板下载 - <a href="./template.xlsx">template.xlsx</a>
