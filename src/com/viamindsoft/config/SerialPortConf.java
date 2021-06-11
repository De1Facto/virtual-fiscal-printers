package com.viamindsoft.config;

import java.nio.file.Path;

public interface SerialPortConf {
    Path fileDescriptor();
    int bufferSize();
}
