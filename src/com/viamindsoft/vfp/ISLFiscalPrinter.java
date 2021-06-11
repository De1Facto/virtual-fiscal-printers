package com.viamindsoft.vfp;

import com.fazecast.jSerialComm.SerialPort;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.CommandFactory;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.UnIdentifiedCommand;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl.*;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalPrinterSerial;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalPrinterSerialImpl;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts.isl.IslCurrentFiscalReceipt;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts.isl.IslCurrentFiscalReceiptImpl;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.Frame;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.isl.IslFrameImpl;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.isl.ISLSingleByteResponse;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.isl.IslDataResponseBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISLFiscalPrinter implements FiscalPrinter {

    private final SerialPort serialPort;
    private final FiscalPrinterData fiscalPrinterData;
    private boolean receiptOpen = false;
    private final Logger logger;
    private OutputStream outputStream;
    private final CommandFactory commandFactory = new IslCommandFactory();
    private final Stack<Command> commandStack = new Stack<>();
    private IslCurrentFiscalReceipt currentFiscalReceipt = null;


    public ISLFiscalPrinter(SerialPort serialPort, FiscalPrinterData fiscalPrinterData, Logger logger) {
        this.serialPort = serialPort;
        this.fiscalPrinterData = fiscalPrinterData;
        this.logger = logger;
    }

    @Override
    public SerialPort serialPort() {
        return serialPort;
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
        verifyCorrectPrinterNetworkNum(bytes);
        Command command = commandFactory.createCommand(bytes);
        logger.log(Level.INFO, "Command is: "+ command.getClass());
        execute(command);
    }

    private void execute(Command command) {
        if(command instanceof IslPrintNumCommand) {
            execute((IslPrintNumCommand) command);
            return;
        }
    }


    private void execute(IslPrintNumCommand command) {
        IslDataResponseBuilder islDataResponseBuilder = new IslDataResponseBuilder();
        islDataResponseBuilder
                .append(fiscalPrinterData.serialNumber())
                .append(fiscalPrinterData.fiscalMemoryNum())
                .append("0",'0',14,true)
                .append(String.valueOf(fiscalPrinterData.lastReceipt()),'0',4,true)
                .append(String.valueOf(fiscalPrinterData.lastInvoiceNum()),'0',10,true)
                .append("11");
        writeToOb(IslFrameImpl.fromResponse(islDataResponseBuilder));
    }
    private void execute(IslOpenReversalReceiptCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        openReceiptAndSetUnp();
        currentFiscalReceipt.openReceipt(command);
        commandStack.push(command);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void verifyThereIsNoError() {
        if(fiscalPrinterData.hasError())
            throw new RuntimeException("PRINTER IS IN ERROR STATE: "+fiscalPrinterData.errorCode());
    }

    private void verifyReceiptIsNotOpen() {
        if(receiptOpen) throw new RuntimeException("CANT EXECUTE WHEN RECEIPT IS OPEN");
    }

    private void verifyReceiptIsOpen() {
        if(!receiptOpen) throw new RuntimeException("CANT EXECUTE WHEN RECEIPT IS NOT OPEN");
    }

    private void verifyLastCommandWasSaleCommand() {
        if(!(commandStack.peek() instanceof IslItemSaleCommand) || !(commandStack.peek() instanceof IslItemReversalCommand))
            throw new RuntimeException("LAST COMMAND WAS NOT A SALE COMMAND");
    }



    private void execute(IslItemReversalCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        if(!(commandStack.peek() instanceof IslOpenReversalReceiptCommand) || !(commandStack.peek() instanceof IslItemReversalCommand)) {
            writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.nack()));
            return;
        }
        openReceiptAndSetUnp();
        currentFiscalReceipt.addItem(command);
        commandStack.push(command);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslItemSaleCommand command) {
        verifyThereIsNoError();
        if(!receiptOpen)
            openReceiptAndSetUnp();
        currentFiscalReceipt.addItem(command);
        commandStack.push(command);
        receiptOpen = true;
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslVoidCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        if(command.getVoidJustLast()) {
            verifyLastCommandWasSaleCommand();
        }
        emptyCommandStack();
        closeReceiptAndClearUnp();
        commandStack.push(command);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslValueDiscountOrSurchargeCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        verifyLastCommandWasSaleCommand();
        currentFiscalReceipt.addDiscountOrSurcharge(command);
        commandStack.push(command);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }
    private void execute(IslPercentDiscountOrSurcharge command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        try {
            verifyLastCommandWasSaleCommand();
            currentFiscalReceipt.addDiscountOrSurcharge(command);
        }catch (RuntimeException exception) {
            verifyLastCommandWasSubtotal();
        }
        commandStack.push(command);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void verifyLastCommandWasSubtotal() {
        if(!(commandStack.peek() instanceof IslSubtotalCommand))
            throw new RuntimeException("LAST COMMAND WAS NOT SALE OR SUBTOTAL");
    }

    private void execute(IslSubtotalCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        currentFiscalReceipt.subtotal(command);
        commandStack.push(command);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslPaymentAndFinishCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        currentFiscalReceipt.addPayment(command);
        commandStack.push(command);
        if(currentFiscalReceipt.isFinished()) {
            fiscalPrinterData.updateTotals(currentFiscalReceipt.paymentsTotals());
            closeReceiptAndClearUnp();
            emptyCommandStack();
        }
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void emptyCommandStack() {
        while (!commandStack.empty())
            commandStack.pop();
    }

    private void execute(IslDailyReportCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        if(! command.getNoReset()) {
            fiscalPrinterData.clearTotals();
            emptyCommandStack();
        }
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslDepositOrWithdrawMoneyCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        Long value = command.getAmount();
        if(command.getWithdraw()) {
            verifyTotalIsLessThanAmountForWithdraw(
                    command.getPaymentType().intValue(),
                    command.getAmount()
            );
            value = -value;
        }
        fiscalPrinterData.addTotal(command.getPaymentType().intValue(),value);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void verifyTotalIsLessThanAmountForWithdraw(int paymentType,Long amount) {
        if(fiscalPrinterData.paymentsTotals().get(paymentType) < Math.abs(amount))
            throw new RuntimeException("CANT WITHDRAW AS MUCH...");
    }

    private void execute(IslSetDateTimeCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslSetOperatorCommand command) {
        verifyThereIsNoError();
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslPrintCommentCommand command) {
        verifyThereIsNoError();
        if(!receiptOpen) {
            openReceiptAndSetUnp();
        }
        currentFiscalReceipt.addComment(command);
        commandStack.push(command);
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslKlenReportCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.ack()));
    }

    private void execute(IslLastReceiptCopyCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        while(!(commandStack.peek() instanceof IslPaymentAndFinishCommand))
            commandStack.pop();
        Frame frame = commandStack.isEmpty()
                ? IslFrameImpl.fromResponse(ISLSingleByteResponse.nack())
                : IslFrameImpl.fromResponse(ISLSingleByteResponse.ack());
        writeToOb(frame);
    }

    private void execute(IslClearErrorsCommand command) {
        Frame frame = fiscalPrinterData.attemptToClearErrors()
                ? IslFrameImpl.fromResponse(ISLSingleByteResponse.ack())
                : IslFrameImpl.fromResponse(ISLSingleByteResponse.nack());
        writeToOb(frame);
    }

    private void execute(IslGetDateTimeCommand command) {
        LocalDateTime dateTime = LocalDateTime.now();
        logger.info("In GetDateTime Procedure");
        IslDataResponseBuilder builder = new IslDataResponseBuilder();
        builder.append(dateTime.format(
                DateTimeFormatter.ofPattern("ddMMyyHHmmss")
        ));
        writeToOb(IslFrameImpl.fromResponse(builder));
    }



    /*private void execute(Command command) {
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.nack()));
    }*/
    private void execute(UnIdentifiedCommand command) {
        writeToOb(IslFrameImpl.fromResponse(ISLSingleByteResponse.nack()));
    }

    private void openReceiptAndSetUnp() {
        fiscalPrinterData.incrementLastReceipt();
        currentFiscalReceipt = new IslCurrentFiscalReceiptImpl(fiscalPrinterData.lastReceipt());
        receiptOpen = true;
    }

    private void closeReceiptAndClearUnp() {
        receiptOpen = false;
        currentFiscalReceipt = null;
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


    private void verifyCorrectPrinterNetworkNum(byte[] inBytes) {
        FiscalPrinterSerial currentSerial = FiscalPrinterSerialImpl.factory(fiscalPrinterData.serialNumber());
        String networkStringFromCommand = parseNetworkStringFromBytes(inBytes);
        String commandString = parseCommandString(inBytes);
        logger.log(Level.INFO,"CurrentSerial: " + currentSerial+ " NetworkStr: "+ currentSerial.networkString());
        logger.log(Level.INFO,"NetworkString: "+networkStringFromCommand);
        logger.log(Level.INFO,"CommandString: "+commandString);
        if(! currentSerial.networkString().equals(networkStringFromCommand) && (!networkStringFromCommand.equals("0000") && !parseCommandString(inBytes).equals("00"))) {
            throw new RuntimeException("KUREC");
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


    @Override
    public FiscalPrinterData fiscalPrinterData() {
        return fiscalPrinterData;
    }


    private @NotNull
    String fillLongUpToCharsWithZeroesUpfront(Long target, int length) {
        String targetString = target.toString();
        return "0".repeat(Math.max(0, (length - targetString.length()))) +
                targetString;
    }
}
