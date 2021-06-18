package com.viamindsoft.main;

import com.fazecast.jSerialComm.SerialPort;
import com.viamindsoft.vfp.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        Path serialDevice = Path.of("/dev/tnt0");
        Logger logger = Logger.getGlobal();
        SerialPort port = SerialPort.getCommPort(serialDevice.toString());
        ISLFiscalPrinter fiscalPrinter = new ISLFiscalPrinter(
                FiscalPrinterDataImpl.factory("IS010101","12345678","ISL5011S-KL"),
                logger
        );
        FiscalDevice device = new ISLFiscalDevice(port,fiscalPrinter,logger);
        device.listen();
    }
}
