# 一致性hash应用-分表扩容demo
#### 之前给项目里的一个5000多万的表做了水平分表，暂时还没有分库的打算，之前用的一致性hash分了32个表，每个表大概百来万数据，还能坚持。虽然还不需要扩容，但是先写个demo后续用到拿来直接用

> hash方法用的md5，虚拟节点每个表插入了128个，扩容的时候如果按照2的倍数扩大，迁移率每个表大概在0.5左右

 用一个TreeMap存放虚拟节点
```
    private static SortedMap<Long, String> virtualMap = new TreeMap<>();
```
 hash方法

 ![](http://chen_tiefeng.gitee.io/cloudimg/img/Clip_20191206_173917.png)
 
 初始化一致性hash，返回统计map
 
 ![](http://chen_tiefeng.gitee.io/cloudimg/img/Clip_20191206_174146.png)
 
 根据id获取表
 
 ![](http://chen_tiefeng.gitee.io/cloudimg/img/Clip_20191206_174331.png)
 
 测试下
 
 ![](http://chen_tiefeng.gitee.io/cloudimg/img/Clip_20191206_174450.png)
 
 结果
 
 ![](http://chen_tiefeng.gitee.io/cloudimg/img/Clip_20191206_174547.png)
 ![](http://chen_tiefeng.gitee.io/cloudimg/img/Clip_20191206_174611.png)
 ![](http://chen_tiefeng.gitee.io/cloudimg/img/Clip_20191206_174645.png)
 