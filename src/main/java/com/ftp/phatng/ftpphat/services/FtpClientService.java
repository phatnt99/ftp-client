package com.ftp.phatng.ftpphat.services;

import java.io.InputStream;

public interface FtpClientService {
    public String getFiles();
    public boolean uploadFile(String path, InputStream inputStream) throws Exception;
}
