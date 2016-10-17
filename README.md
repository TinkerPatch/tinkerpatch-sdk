# [Tinker](https://github.com/Tencent/tinker) Client

[ ![Download](https://api.bintray.com/packages/simsun/maven/TinkerClient-okhttp-integration/images/download.svg) ](https://bintray.com/simsun/maven/TinkerClient-okhttp-integration/_latestVersion)

This is project is in very alpha stage, feel free to make a PR. 

## Download


```gradle
repositories {
  jcenter()
}

dependencies {
  compile 'com.github.simpleton:tkclient:0.0.4@aar'
}

```

## Backend

http://tinkerpatch.com


## Support Integration

we use urlConnection as default. and you support importing some fancy httpClient as integrations.  

### 1. okhttp


add `compile 'com.github.simpleton:okhttp-integration:0.0.4@aar'` in your `dependencies`

### 2. okhttp3

add `compile 'com.github.simpleton:okhttp3-integration:0.0.4@aar'` in your `dependencies`

###3. volly(TODO)
###4. apachehttpclient(TODO)

