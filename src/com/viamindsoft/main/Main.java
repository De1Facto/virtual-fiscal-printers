package com.viamindsoft.main;

import com.fazecast.jSerialComm.SerialPort;
import com.viamindsoft.vfp.FiscalPrinter;
import com.viamindsoft.vfp.FiscalPrinterDataImpl;
import com.viamindsoft.vfp.ISLFiscalPrinter;

import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Path serialDevice = Path.of("/dev/tnt0");
        Logger logger = Logger.getGlobal();
        SerialPort port = SerialPort.getCommPort(serialDevice.toString());
        FiscalPrinter fiscalPrinter = new ISLFiscalPrinter(
                port,
                FiscalPrinterDataImpl.factory("IS010101","12345678","ISL5011S-KL"),
                logger
        );
        fiscalPrinter.listen();
    }
}
