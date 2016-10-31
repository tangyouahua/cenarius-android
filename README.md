# Cenarius-Android

**Cenarius** 是一个针对移动端的混合开发框架。现在支持 Android 和 iOS 平台。`Cenarius-Android` 是 Cenarius 在 Android 系统上的客户端实现。

通过 Cenarius，你可以使用包括 javascript，css，html 在内的传统前端技术开发移动应用。Cenarius 的客户端实现 Cenarius Container 对于 Web 端使用何种技术并无要求。

Cenarius-Android 现在支持 Android 4.1 及以上版本。


## Cenarius 简介

Cenarius 包含三个库：

- Cenarius Web ：[https://github.com/macula-projects/cenarius-web](https://github.com/macula-projects/cenarius-web)。

- Cenarius iOS[https://github.com/macula-projects/cenarius-ios](https://github.com/macula-projects/cenarius-ios)。

- Cenarius Android：[https://github.com/macula-projects/cenarius-android](https://github.com/macula-projects/cenarius-android)。

## 安装

### 安装 Jitpack

[![](https://jitpack.io/v/macula-projects/cenarius-android.svg)](https://jitpack.io/#macula-projects/cenarius-android)

## 使用

你可以查看 Demo 中的例子。了解如何使用 Cenarius。Demo 给出了完善的示例。

### 配置

#### 初始化

在Application的`onCreate`中调用

```Java
  Cenarius.initialize(this);
```

#### 设置路由表文件 api

```Java
  RouteManager.getInstance().setRemoteFolderUrl("http://172.20.70.80/www");
```

Cenarius 使用 uri 来标识页面。提供一个正确的 uri 就可以创建对应的 CNRSViewActivity。路由表提供了每个 uri 对应的 html 资源的哈希值。Demo 中的路由表如下：

```json
[
  {
    "uri": "build/index.html",
    "hash": "8c16c85d8e2ca8b7088c68be17ea8b61"
  },
  {
    "uri": "build/index.js",
    "hash": "70c84eebd1833611da8dd65d5dedc3ff"
  }
]
```

#### 预置资源文件路径

预置文件路径是`assets/www`

使用 Cenarius 一般会预置一份路由表，以及资源文件在应用包中。这样就可以减少用户的下载，加快第一次打开页面的速度。在没有网络的情况下，如果没有数据请求的话，页面也可访问。这都有利于用户体验。

#### 开发模式

```java
    Cenarius.setDevelopModeEnable(true);
```

开发模式允许 H5 人员通过替换文件的方式迅速更新文件。
启用开发模式后，路由功能将失效。url 加载目录为

```
SD/<APPName>/www
```

### 使用 CNRSViewActivity

你可以直接使用 `CNRSViewActivity` 作为你的混合开发客户端容器。或者你也可以继承 `CNRSViewActivity`，在 `CNRSViewActivity` 基础上实现你自己的客户端容器。

## 使用 CNRSWebViewActivity 和 CNRSCordovaActivity

CNRSWebViewActivity 和 CNRSCordovaActivity 继承于 CNRSViewActivity。

CNRSWebViewActivity 提供基础的 html 容器功能。CNRSCordovaActivity支持Cordova功能。实际开发中应按照需求选择。

为了初始化 CNRSWebViewActivity 和 CNRSCordovaActivity，你只需要一个 uri。在路由表中可以找到这个 uri。这个 uri 标识了该页面所需使用的资源文件的位置。Cenarius Container 会通过 uri 在路由表中寻找对应的资源文件。

```java
openWebPage("build/index.html", null);
openCordovaPage("build/index.html", null);
```

## 定制你自己的 Cenarius Container

首先，可以继承 `CNRSWebViewActivity` 或 `CNRSCordovaActivity`，在此基础上以实现你自己客户端容器。

我们暴露了三类接口。供开发者更方便地扩展属于自己的特定功能实现。

### 定制 CNRSWidget

Cenarius Container 提供了一些原生 UI 组件，供 Cenarius Web 使用。CNRSWidget 是一个 Java 协议（Protocol）。该协议是对这类原生 UI 组件的抽象。如果，你需要实现某些原生 UI 组件，例如，弹出一个 Toast，或者添加原生效果的下拉刷新，你就可以实现一个符合 CNRSWidget 协议的类，并实现以下方法：`getPath:`, `handle:`。

在 Demo 中可以找到一个例子：`TitleWidget` ，通过它可以设置导航栏的标题文字。

```java
public class TitleWidget implements CenariusWidget {
    
    static final String KEY_TITLE = "title";

    @Override
    public String getPath() {
        return "/widget/nav_title";
    }

    @Override
    public boolean handle(WebView view, String url) {
        HashMap dataMap = GsonHelper.getDataMap(url, getPath());
        if (dataMap != null){
            if (null != view && view.getContext() instanceof Activity) {
                ((Activity) view.getContext()).setTitle((String) dataMap.get(KEY_TITLE));
            }
            return true;
        }
        return false;
    }
}
```

## License

Cenarius is released under the MIT license. See LICENSE for details.
