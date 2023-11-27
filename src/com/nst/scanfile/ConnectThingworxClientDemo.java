package com.nst.scanfile;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.IPasswordCallback;
import com.thingworx.communications.client.things.VirtualThing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;


/**
 *
 */
public class ConnectThingworxClientDemo {
    static String appKey ;          //成员静态变量(类变量)  appKey;
    static String twUri;            //成员静态变量    thingworx平台访问URI
    static String remoteThingName;  //成员静态变量    remoteThing的名字

    private final static Logger LOG = LoggerFactory.getLogger(ConnectThingworxClientDemo.class);        //常量

    public static void main(String[] args){
        InputStream resourceAsStream;
        try{
            //读取配置文件
            resourceAsStream = ConnectThingworxClientDemo.class.getClassLoader().getResourceAsStream("scanner.properties");

            //整理配置项
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            //关闭文件读取通道
            resourceAsStream.close();

            twUri = properties.getProperty("thingworx_ws_uri");
            appKey = properties.getProperty("app_key");
            remoteThingName = properties.getProperty("remote_thing_name");

            if ("".equals(remoteThingName)) {
                //LOG.error("远程事物的名字不能为空");
                throw new Exception("远程事物的名字不能为空!!");

            }

            //客户端的配置
            ClientConfigurator config = new ClientConfigurator();
            config.setUri(twUri);
            config.ignoreSSLErrors(true);       //忽略SSL加密协议的报错检测

            //利用一个AppKey来设置安全机要
            IPasswordCallback appKeyCB = new SamplePasswordCallback(appKey);   //v7.0.0以后使用的方式
            //SecurityClaims claims = SecurityClaims.fromAppKeyCallback(appKeyCB);   //v7.0.0以下实现的方式
            config.setSecurityClaims(appKeyCB);       //appKey安全认证
            //创建一个客户端实例
            ConnectedThingClient demoClient = new ConnectedThingClient(config);

            //创建一个虚拟Thing 与 Thingworx创建关联
            VirtualThing deviceVThing = new VirtualThing(remoteThingName, "利用Edge SDK 创建的一个远程事物", demoClient);
            //完成与客户端实例的绑定
            demoClient.bindThing(deviceVThing);

            try{
                //连接客户端实例至
                demoClient.start();

                //暂停5秒让Thingworx可以接收到发送的请求
                //Thread.sleep( 5 * 1000);
                LOG.info("客户端连接开始！");




            }catch(Exception eStart){
                LOG.info("客户端连接失败！-->" + eStart.getMessage());
                eStart.printStackTrace();
            }

            while(!demoClient.isShutdown()){
                //当连接没有断开
                if(demoClient.isConnected()){
                    for (VirtualThing thing : demoClient.getThings().values()) {
                        try {
                            thing.processScanRequest();
                        } catch (Exception eProcessing) {
//                                LOG.error("Error Processing Scan Request for [" + thing.getName() + "] : " + eProcessing.getMessage());
                            eProcessing.printStackTrace();
                        }
                    }
                }
                Thread.sleep(5 * 1000);
            }




        }catch(Exception e){
            LOG.error("客户端实例化失败！" + e.getMessage());
            e.printStackTrace();

        }
    }

}

class SamplePasswordCallback implements IPasswordCallback{
    private String appKey;

    public SamplePasswordCallback(String configAppKey) {
        this.appKey = configAppKey;
    }

    @Override
    public char[] getSecret() {
        return appKey.toCharArray();
    }
}
