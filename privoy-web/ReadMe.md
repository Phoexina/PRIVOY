## 生成并导入主秘密：
```
    FileLoader.loadIdentity (Long 用户ID, String APP文件存储路径);
    //静态函数，如主秘密文件已存在，且ID与参数ID一致，则直接导入文件；否则重新生成文件
    //APP文件存储路径可以参考在主页面获取getApplicationContext().getFilesDir().getAbsolutePath()
```
## 获取生成的主秘密文件：
```
    PathUtil.BASE=getApplicationContext().getFilesDir().getAbsolutePath();
    File file = PathUtil.getKeysFile();
    //如此前未调用loadIdentity，需先为PathUtil.BASE赋值
```
## 上传主秘密：
```
    int res=FileLoader.uploadKeyFile()
    //静态函数，自动获取主秘密文件上传
    //return int:0:成功上传; -1：文件不存在; 1：连接失败
```
*修改ip地址和端口（如下数据为默认设置）：
```
    FileUpload.HOST="59.110.115.66";
    FileUpload.TCP_PORT=8080;
```
---
## 发送密文初始化：
```
    CiphertextTransformation transformation = CiphertextTransformation.getInstance();
    //CiphertextTransformation单例模式，负责发送密文的唯一实例

    Window firstWindow = transformation.startsubmit(Long 密文属性ID);
    //return Window:(Long windowStart, Long windowEnd)
    //发送某属性密文前需要初始化属性窗口，获取首个窗口，需等待到窗口开始时间，才能开始发送密文
    //等待参考：Thread.sleep(Math.max(0L, firstWindow.getStart() - System.currentTimeMillis()));
```
*修改ip地址和端口（如下数据为默认设置）：
```
    CiphertextTransformationFacade.HOST="8.130.10.212";
    CiphertextTransformationFacade.TCP_PORT=9009;
```

## 发送密文（一条）：
```
    Input input =new Input(Long value, Long count);
    //Input为明文数据，value为接受后累加值，count为本次计数次数

    transformation.submit(Long 密文属性ID,Input input, Long 当前时间戳);
    //在初始化且网络正常情况下，该函数将自动更新窗口、加密明文数据并发送
```

## 结束密文发送：
```
    transformation.closesubmit();
    //密文传送设置了中断重连，因此当确认不再需要发送密文时，需告知实例不再重连
    //结束发送后，如再次需要发送，则需重新初始化
```
