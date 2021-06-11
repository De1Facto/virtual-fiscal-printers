package com.viamindsoft.vfp;

import com.fazecast.jSerialComm.SerialPort;

public interface FiscalDevice {
     void listen();
     FiscalPrinter fiscalPrinter();
     SerialPort serialPort();
}
