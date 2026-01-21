package com.innerken.aadenprinterx;

interface IPrinterStatusCallback {
    //小黑盒连接状态变化通知
    void onStatusChanged(int state);
}