# PRIVOY 
## <font color= 3399FF>安卓客户端</font>
### 主秘密部分
生成并导入主秘密：  
调用静态函数loadIdentity，userID为Long类型，basePath为String类型。如主秘密文件已存在，且ID与参数ID一致，则直接导入文件；否则重新生成文件。  
APP文件存储路径可以参考在主页面获取
```java
    String basePath = getApplicationContext().getFilesDir().getAbsolutePath();
    FileLoader.loadIdentity (userID, basePath);
```
获取生成的主秘密文件：  
调用静态函数getKeysFile()，如此前未调用loadIdentity，需先为PathUtil.BASE赋值
```java
    PathUtil.BASE = getApplicationContext().getFilesDir().getAbsolutePath();
    File file = PathUtil.getKeysFile();
```
上传主秘密：  
调用静态函数uploadKeyFile()，自动获取主秘密文件上传，返回值为int，0:成功上传; -1：文件不存在; 1：连接失败。
```java
    int res=FileLoader.uploadKeyFile()
```
连接我们的服务器（维护用户主秘密并管理token）：  
修改上传主秘密的ip地址和端口，如下数据为默认设置）
```java
    FileUpload.HOST="59.110.115.66";
    FileUpload.TCP_PORT=8080;
```
### 发送密文部分
发送密文初始化：  
CiphertextTransformation单例模式，负责发送密文的唯一实例。  
发送某属性密文前需要初始化属性窗口，获取首个窗口，需等待到窗口开始时间，才能开始发送密文。调用初始化后CiphertextTransformation类型的startsubmit函数，universeId为与我们协商好的数据类型id，返回值为初始窗口Window:(Long windowStart, Long windowEnd)。  
如需等待到窗口的start可参考，Thread.sleep。
```java
    CiphertextTransformation transformation = CiphertextTransformation.getInstance();
    Window firstWindow = transformation.startsubmit(Long universeId);
    Thread.sleep(Math.max(0L, firstWindow.getStart() - System.currentTimeMillis()));
```
连接数据服务器中转（转发至kafka处理）：  
修改数据服务器中转程序的ip地址和端口，如下数据为默认设置
```java
    CiphertextTransformationFacade.HOST="8.130.10.212";
    CiphertextTransformationFacade.TCP_PORT=9009;
```
发送密文（一条）：
```java
    Input input =new Input(Long value, Long count);
    //Input为明文数据，value为接受后累加值，count为本次计数次数

    transformation.submit(Long 密文属性ID,Input input, Long 当前时间戳);
    //在初始化且网络正常情况下，该函数将自动更新窗口、加密明文数据并发送
```

## 结束密文发送：
```java
    transformation.closesubmit();
    //密文传送设置了中断重连，因此当确认不再需要发送密文时，需告知实例不再重连
    //结束发送后，如再次需要发送，则需重新初始化
```
