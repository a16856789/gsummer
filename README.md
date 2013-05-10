GSummer
=======

* 包含：
   * 数据库访问框架, 基于Hibernate
   * MVC框架

## 配置文件 app.properties

    app.package  = xxx # 应用程序的包前缀
    app.homepage = xxx # 应用程序的首页

## 数据库访问框架GORM

### 配置文件 hibernate.cfg.xml

```xml
<property name="connection.driver_class">com.mysql.jdbc.Driver</property>
<property name="connection.url">jdbc:mysql://localhost:3306/db</property>
<property name="connection.username">root</property>
<property name="connection.password">root</property> 
```

重要的配置项：

配置项                  | 作用
------------------------|-----------
connection.driver_class | JDBC驱动类
connection.url          | URL地址
connection.username     | 用户名
connection.password     | 密码

### 实体类

简单的理解，一个实体类对应数据库中的一张表。

项目          | 解释
--------------|---------------------
@Entity       | 表示该类是一个实体类
@Id           | id是其主键
@ManyToOne    | 一个一对多的关系
LAZY          | 延迟加载
length = 1024 | 设置该列的长度为1024

### GDao

GDao是框架提供的一全局唯一的DAO类，其所有的方法都是静态的。GDao封装了Hibernate中常用的方法，使得访问数据库更简单，易用。

GDao中的大部分方法都会附加到实体类中，比如从数据库读取一行记录的get方法可以这么使用：

	def comment = Commment.get(1L);

相关的方法列表：

方法名          | 修饰   | 说明
----------------|--------|---------------------------------------------
get             | static | 根据ID载入记录
save            |        | 保存当前记录
update          |        | 更新当前记录
merge           |        | 更新当前记录，支持级联以及非持久化实体的更新
delete          |        | 删除当前记录
executeUpdate   | static | 执行Update语句
find            | static | 利用Select语句从数据库中查找单条记录
findAll         | static | 利用Select语句从数据库中查找多条记录
where           | static | findAll的语法糖
loadAll         | static | 载入所有的记录
withTransaciton | static | 执行一个事务

#### get详解

#### findAll详解

	List findAll(String hql, Object...values, max: Integer, offset: Integer, offset: Integer, scroll: Boolean, Colsure callback);

参数：

* hql: 需要执行的HQL语句，可以用`?`表示参数
* values: HQL中`?`参数的值
* max: 最大选出的记录数
* offset: 第一条记录的偏移
* scroll: 是否使用滚动记录集，如果为true，那么用callback迭代记录集，且findAll不返回结果
* callback: 如果非空，则用该callback函数迭代所有的结果记录。如果scroll为true，则必须指定callback函数

#### executeUpdate详解

## MVC框架GWeb

### web.xml中的配置

### 控制类

### 控制方法

### 视图


