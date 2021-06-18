package com.viamindsoft.vfp;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;

public interface FiscalDevice {
     void listen();
     FiscalPrinter fiscalPrinter();
     SerialPort serialPort();
     void handle(SerialPortEvent event);
}
