# 这是一个安卓实习项目
## 实习第一天
完成git仓库配置、android studio配置，并且初步摸索android项目架构



## 实习第二天

Android开发四大组件：Activity、Service、broadcast recevier、conten provider

Activity——核心页面组件，学习内容包括创建、生命周期、启动模式以及Intent控制

Fragment——为解决activity资源占用问题，用于模块化界面开发的小型页面组件，主要学习引入、控制器manager、事务traction、生命周期、通信机制、动画





## 实习第三天

Activity中常用的视图组件

- view，基本视图组件，实现常见的一些位置、尺寸等控制
- TextView，继承view，实现文本效果，增加文本相关控制
- Button，继承view，实现按钮事件监控机制



Activity中布局相关

- 布局类型，包括传统5大布局（框架、线性、相对、表格、绝对）和新型布局（ConstrainLayout）
- 布局实现解析：setContentView->inflate,获取并解析xml，创建view，反射构造View对象、设置view对象、构建view树、添加到ContentParent
- 布局优化：使用include、group等方式创建复合组件，实现高效组件复用，一定程度上损失了性能效果





## 实习第四天

介绍安卓开发四大组件：Activity、Service、BroadcastReciever、ContenProvider

Activity重点说明实际生命周期管理、intent通信连接方式

Service负责非用户交互的后台服务程序，基本实现需要继承实现Service类并且在注册文件中声明。

Service核心介绍通信实现机制，通过Binder机制管理资源与共享（零拷贝），通过实例化Binder对象获取Service对象，实现进程内通信；通过AIDL方式获取Service代理对象，实现跨进程通信

BroadcastReciever消息广播者负责监听与发布消息，实现方式分为静态广播和动态广播，静态需要在注册文件中注册声明，动态编写BroadcastReciever继承类，重写相关监听方法。消息广播者在对应监听对象销毁时需要显式销毁，防止资源损耗

ContenProvider内容提供者负责提供数据服务，在注册文件中声明并实现ContenProvider继承类，进行数据操作







## 实习第五天

安卓权限管控相关概念，按实现分为动态权限管控和静态权限管控，按管控级别分为普通管控、敏感管控（危险管控）、系统管控

网络通信实现基于okhttp客户端方式和Retrofit编程增强网络通信方案

学习Handler机制，主要包括Message、MessageQueue、Looper、Handler，处理机制类似消息队列机制，Looper管理消息队列（线程唯一），Handler绑定Looper并进行消息的发送和接收处理。Handler机制实现了线程间非阻塞通信，对于一些耗时任务或者复杂后台任务需要实时更新UI主线程具有很大的帮助

本地存储方案，包括sharedPreferences、MMKV、SQLite存储方案，当前安卓系统默认SQLite引擎，通过相关依赖库或者SQLite3命令行工具进行本地数据库管理





## 实习第六天

主要介绍安卓常用的组件库：


1.依赖引入方法：公共仓库引入/自定义组件库本地引入

2.图片库方法：Glid，支持多种图片处理效果，核心内容包括介绍图片缓存机制、核心线程池、图片生命周期管理机制

3.视频库方法：player

4.持久化存储：MMKV组件，核心机制采用mmap映射技术，实现零拷贝快速访问存储

5.瀑布流布局：灵活的布局方案，采用StafferedGridLayoutManager布局管理器实现，注意保证列宽

6.适配器组件：适配器是视图组件与数据进行更新交互的工具

7.事件传递组件：EventBus







