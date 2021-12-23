# ZBus

[![](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html) [![API](https://img.shields.io/badge/API-19+-blue.svg?style=flat-square)](https://developer.android.com/about/versions/android-4.0.html)[![License](http://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0)

#### A Light Event Bus For Android. 轻量级事件发布/订阅框架，支持生命周期感知，支持粘性事件，支持绑定TAG，支持绑定View，可传递多个参数。

## 一、安装（TODO 发布库到jitpack）

#### Latest Version：1.0.0
```groovy
    implementation 'com.github.Z-P-J:ZBus:1.0.0'

```
## 二、使用（[查看 demo](https://github.com/Z-P-J/ZBus/tree/master/app)）

### 1. 订阅和发送自定义Event类型事件

```java
    // 订阅自定义的Event类型事件
    ZBus.with(LifecycleOwner)
        .observe(Event.class)
        .bindTag("TAG") // 绑定TAG，可根据该TAG移除订阅
        .bindView(tvText) // 绑定View，当View销毁时自动取消订阅
        .bindLifecycle(Owner, Lifecycle.Event.ON_PAUSE) // 绑定Activity/Fragment生命周期
        .doOnAttach(new Runnable() {
            @Override
            public void run() {
                // 开始监听
            }
        })
        .doOnChange(new Consumer<Event>() {
            @Override
            public void accept(Event event) {
                // 接收Event类型的信息
            }
        })
        .doOnDetach(new Runnable() {
            @Override
            public void run() {
                // 移除监听
            }
        })
        .subscribe();

    // 发送Event类型事件
    ZBus.post(new Event());
```

### 2. 订阅和发送Key事件

```java
    // 订阅Key事件
    ZBus.with(lifecycleOwner)
        .observe("Key")
        .doOnChange(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                // 收到信息
            }
        })
        .subscribe();

    // 发送Key事件
    ZBus.post("Key");
```

### 3. 订阅和发送Key and Event类型事件

```java
    // 订阅者1：订阅Key1和Boolean类型事件
    ZBus.with(lifecycleOwner)
        .observe("Key1", Boolean.class)
        .doOnChange(new SingleConsumer<Boolean>() {
            @Override
            public void onAccept(Boolean event) throws Exception {
                // 收到event1
            }
        })
        .subscribe();

    // 订阅者2：订阅Key2和Boolean类型事件
    ZBus.with(lifecycleOwner)
        .observe("Key2", Boolean.class)
        .doOnChange(new SingleConsumer<Boolean>() {
            @Override
            public void onAccept(Boolean event) throws Exception {
                // 收到event
            }
        })
        .subscribe();

    // 订阅者3：订阅Key1和Integer类型事件
    ZBus.with(lifecycleOwner)
        .observe("Key1", Integer.class)
        .doOnChange(new SingleConsumer<Integer>() {
            @Override
            public void onAccept(Integer event) throws Exception {
                // 收到event
            }
        })
        .subscribe();

    // 发送Key and Event事件
    ZBus.post("Key1", true); // 订阅者1将收到事件
    ZBus.post("Key2", false); // 订阅者2将收到事件
    ZBus.post("Key1", 1); // 订阅者3将收到事件
```

### 4. 传送两个或三个参数，减少自定义Event类

```java
    // 当我们要同时传送两个参数时我们无需自定义新的Event类，可直接使用以下方法
    ZBus.with(lifecycleOwner)
        .observe("Key", Boolean.class, Integer.class)
        .doOnChange(new ZBus.PairConsumer<Boolean, Integer>() {
            @Override
            public void onAccept(Boolean b, Integer i) {
                // 收到信息: b=true and i=100
            }
        })
        .subscribe();

    // 发送Key事件
    ZBus.post("Key", true, 100);

	// 当我们要同时传送三个参数时我们无需自定义新的Event类，可直接使用以下方法
    ZBus.with(lifecycleOwner)
        .observe("Key", String.class, Boolean.class, Double.class)
        .doOnChange(new ZBus.TripleConsumer<String, Boolean, Integer>() {
            @Override
            public void onAccept(String s, Boolean b, Double d) {
                // 收到信息: s="msg" and b=false and d=100.0
            }
        })
        .subscribe();

    // 发送数据
    ZBus.post("Key", "msg", false, 100.0);

	// 注意：若发送以下数据上面的订阅者将接收不到，第三个参数必须是Double类型才能接收到
	ZBus.post("Key", "msg", false, 100);
```

### 5. 粘性事件(Sticky Event)

```java
    /*
    支持Sticky事件，ZBus.withSticky和ZBus.postSticky，其他用法同上
    */

    // 订阅Sticky事件
    ZBus.withSticky()
        .observe(...)

    // 发送Sticky事件
    ZBus.postSticky(...);

    // 获取Sticky事件
	ZBus.getStickyEvent("key");
	ZBus.getStickyEvent(clazz);
	ZBus.getStickyEvent("key", clazz);

	// 移除Sticky事件
	ZBus.removeStickyEvent("key");
	ZBus.removeStickyEvent(clazz);
	ZBus.removeStickyEvent("key", clazz);
	ZBus.removeAllStickyEvents();
```

### 6. ZBus生命周期

```java
    /*
    订阅者1、2和3都依赖于object1，订阅者4依赖于object2
    */

    // 订阅者1
    ZBus.with(object1)
        .observe("Key1", Event1.class)
        .bindTag("TAG1") // 绑定TAG，可根据该TAG移除订阅
        .bindView(bindView) // 绑定View，当View销毁时自动取消订阅
        .bindLifecycle(this, Lifecycle.Event.ON_PAUSE) // 绑定Activity / Fragment的onPause生命周期
        .bindLifecycle(this) // 绑定Activity / Fragment生命周期，默认为onDestroy时移除取消订阅
        .subscribe();

    // 订阅者2
    ZBus.wtih(object1)
        .observe("Key2", Event1.class)
        .bindTag("TAG2")
        .subscribe();

    // 订阅者3
    ZBus.with(object1)
        .observe("Key1", Event2.class)
        .bindTag("TAG3")
        .subscribe();

    // 订阅者4
    ZBus.with(object2)
        .observe("Key1", Event2.class)
        .bindTag("TAG3")
        .subscribe();

    // 移除bindView，当bindView调用onViewDetachedFromWindow时将自动取消订阅者1
    view.removeView(bindView); 

    // 当Activity/Fragment onPause时将自动取消订阅者1
    onPause();
	
	// 根据依赖的Object取消订阅
    ZBus.removeObservers(object1); // 订阅者1、订阅者2和订阅者3都会被取消
    ZBus.removeObservers(object2); // 只取消订阅者4

	// 根据TAG取消订阅
    ZBus.removeObservers("TAG1"); // 只取消订阅者1
    ZBus.removeObservers("TAG2"); // 只取消订阅者2
    ZBus.removeObservers("TAG3"); // 将取消订阅者3和订阅者4

	// 根据Key取消订阅
	ZBus.removeObservers("Key1"); // 将取消订阅者1、订阅者3和订阅者4
    ZBus.removeObservers("Key2"); // 只取消订阅者2
```

## 三、License

```
   Copyright 2021 Z-P-J

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
