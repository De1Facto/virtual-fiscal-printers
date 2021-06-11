package com.viamindsoft.vfp;

import com.fazecast.jSerialComm.SerialPort;

public interface FiscalPrinter  {
    SerialPort serialPort();
    void listen();
    FiscalPrinterData fiscalPrinterData();
}
