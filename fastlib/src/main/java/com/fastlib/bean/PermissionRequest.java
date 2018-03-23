package com.fastlib.bean;

public class PermissionRequest{
    public int requestCode;
    public Runnable hadPermissionProcess;
    public Runnable deniedPermissionProcess;

    public PermissionRequest(int requestCode, Runnable hadPermissionProcess, Runnable diniedPermissionProcess) {
        this.requestCode = requestCode;
        this.hadPermissionProcess = hadPermissionProcess;
        this.deniedPermissionProcess = diniedPermissionProcess;
    }
}