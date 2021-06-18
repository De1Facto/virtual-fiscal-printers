package com.viamindsoft.vfp;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.viamindsoft.shared.SerialDeviceDataListener;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.CommandFactory;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl.IslCommandFactory;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalPrinterSerial;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalPrinterSerialImpl;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.Frame;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.isl.IslFrameImpl;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.isl.ISLSingleByteResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISLFiscalDevice implements FiscalDevice {
    private final SerialPort serialPort;
    private final ISLFiscalPrinter fiscalPrinter;
    private final Logger logger;
    private OutputStream outputStream;
    private final CommandFactory commandFactory;

    public ISLFiscalDevice(SerialPort serialPort, ISLFiscalPrinter fiscalPrinter, Logger logger) {
        this.serialPort = serialPort;
        this.fiscalPrinter = fiscalPrinter;
        this.logger = logger;
        this.commandFactory = new IslCommandFactory(fiscalPrinter);
    }

    @Override
    public void listen() {
        serialPort.openPort();
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING,SerialPort.TIMEOUT_NONBLOCKING,SerialPort.TIMEOUT_NONBLOCKING);
        serialPort.setFlowControl(0);
        serialPort.setParity(0);
        serialPort.setNumStopBits(1);
        serialPort.setBaudRate(115200);
        serialPort.addDataListener(new SerialDeviceDataListener(this));
        outputStream = serialPort.getOutputStream();
    }

    public void handle(SerialPortEvent event) {
        logger.log(Level.INFO, "Available bytes: "+ event.getReceivedData().length);
        byte[] readNBytes = event.getReceivedData();
        logger.log(Level.INFO, "<< " + Arrays.toString(readNBytes));
        parseAndExecuteCommand(readNBytes);
    }


    private void parseAndExecuteCommand(byte[] bytes) {
        Command command = commandFactory.createCommand(bytes);
        logger.log(Level.INFO, "Command is: "+ command.getClass());
        try {
            verifyCorrectPrinterNetworkNum(bytes);
            command.execute();
            writeToOb();
        }catch (RuntimeException e) {
            logger.log(Level.WARNING, e.getMessage());
            writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.nack()));
        }
    }
    private void verifyCorrectPrinterNetworkNum(byte[] inBytes) {
        FiscalPrinterSerial currentSerial = FiscalPrinterSerialImpl.factory(fiscalPrinter.fiscalPrinterData().serialNumber());
        String networkStringFromCommand = parseNetworkStringFromBytes(inBytes);
        if(! currentSerial.networkString().equals(networkStringFromCommand)
                && (!networkStringFromCommand.equals("0000") && !parseCommandString(inBytes).equals("00"))) {
            throw new RuntimeException("Incorrect Printer Network Number");
        }
    }
    private String parseNetworkStringFromBytes(byte[] bytes) {
        int start = 0; int length = 4;
        if(bytes[0] == 0x02) start+=1;
        StringBuilder sb = new StringBuilder();
        for(var i = start; i < (start + length) ; i++) {
            sb.append((char) bytes[i]);
        }
        return sb.toString();
    }
    private String parseCommandString(byte[] bytes) {
        int start = 4; int length = 2;
        if(bytes[0] == 0x02) start+=1;
        StringBuilder sb = new StringBuilder();
        for(var i = start; i < (start + length) ; i++) {
            sb.append((char) bytes[i]);
        }
        return sb.toString();
    }
    private void writeToOb() {
        writeToOb(IslFrameImpl.fromResponse(fiscalPrinter.getResponse()));
    }
    private void writeToOb(Frame frame) {
        writeToOb(frame.toBytes());
    }


    private void writeToOb(byte[] bytes) {
        try {
            outputStream.write(bytes);
            outputStream.flush();
            logger.log(Level.INFO, ">> " + Arrays.toString(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FiscalPrinter fiscalPrinter() {
        return fiscalPrinter;
    }

    @Override
    public SerialPort serialPort() {
        return serialPort;
    }
}
