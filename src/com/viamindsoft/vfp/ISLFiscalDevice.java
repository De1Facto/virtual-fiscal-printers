package com.viamindsoft.vfp;

import com.fazecast.jSerialComm.SerialPort;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.CommandFactory;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl.IslCommandFactory;
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
        try(InputStream in = serialPort.getInputStream()) {
            outputStream = serialPort.getOutputStream();
            while (true) {
                if(in.available() > 0) {
                    logger.log(Level.INFO, "Available bytes: "+ in.available());
                    byte[] readNBytes = in.readNBytes(in.available());
                    logger.log(Level.INFO, "<< " + Arrays.toString(readNBytes));
                    //if(!port.isOpen()) port.openPort();
                    parseAndExecuteCommand(readNBytes);
                    //out.write(outputBytes);

                }
            }
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, e.getMessage());
            writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.nack()));
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } finally {
            serialPort.closePort();
        }
    }

    private void parseAndExecuteCommand(byte[] bytes) {
        Command command = commandFactory.createCommand(bytes);
        logger.log(Level.INFO, "Command is: "+ command.getClass());
        command.execute();
        writeToOb();
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
