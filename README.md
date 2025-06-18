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







