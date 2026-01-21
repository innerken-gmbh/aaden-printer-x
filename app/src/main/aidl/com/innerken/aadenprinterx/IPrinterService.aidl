package com.innerken.aadenprinterx;
import com.innerken.aadenprinterx.IPrinterStatusCallback;

interface IPrinterService {
    //用于心跳检测，服务存活即返回true
    boolean ping();

    //注册打印服务状态回调
    void registerStatusCallback(IPrinterStatusCallback callback);

    //注销状态回调
    void unregisterStatusCallback(IPrinterStatusCallback callback);
}