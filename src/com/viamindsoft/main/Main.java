package com.viamindsoft.main;

import com.fazecast.jSerialComm.SerialPort;
import com.viamindsoft.vfp.*;

import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        for (var i = 0; i < 8; i+=2) {
            startPrinter(i);
        }
    }

    private static void startPrinter(int i) {
        Path serialDevice = Path.of("/dev/tnt"+i);
        Logger logger = Logger.getGlobal();

        SerialPort port = SerialPort.getCommPort(serialDevice.toString());
        String serialNumber = makeSerialNumber(i+1);
        logger.info("Starting printer with serial: "+ serialNumber+" at device: /dev/tnt"+i);
        ISLFiscalPrinter fiscalPrinter = new ISLFiscalPrinter(
                FiscalPrinterDataImpl.factory(serialNumber,"12345678","ISL5011S-KL"),
                logger
        );
        FiscalDevice device = new ISLFiscalDevice(port,fiscalPrinter,logger);
        device.listen();
    }
    private static String makeSerialNumber(Integer i) {
        if(i > 999999) throw new RuntimeException("INVALID SERIAL");
        StringBuilder sb = new StringBuilder("IS");
        int j =0;
        while (j < 6) {
            sb.append(i);
            j += i.toString().length();
        }
        return sb.toString();
    }
}
