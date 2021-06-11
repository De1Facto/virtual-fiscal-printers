package com.viamindsoft.vfp;


import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.UnIdentifiedCommand;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl.*;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalPrinterSerial;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalPrinterSerialImpl;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts.isl.IslCurrentFiscalReceipt;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts.isl.IslCurrentFiscalReceiptImpl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.Response;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.isl.ISLSingleByteResponse;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.isl.IslDataResponseBuilder;
import org.jetbrains.annotations.NotNull;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISLFiscalPrinter implements FiscalPrinter {


    private final FiscalPrinterData fiscalPrinterData;
    private boolean receiptOpen = false;
    private final Logger logger;
    private Response outputBuffer;

    private final Stack<Command> commandStack = new Stack<>();
    private IslCurrentFiscalReceipt currentFiscalReceipt = null;


    public ISLFiscalPrinter(FiscalPrinterData fiscalPrinterData, Logger logger) {
        this.fiscalPrinterData = fiscalPrinterData;
        this.logger = logger;
    }





    public void execute(Command command) {

    }

    @Override
    public Response getResponse() {
        Response response = outputBuffer;
        outputBuffer = null;
        return response;
    }


    public void execute(IslPrintNumCommand command) {
        IslDataResponseBuilder islDataResponseBuilder = new IslDataResponseBuilder();
        islDataResponseBuilder
                .append(fiscalPrinterData.serialNumber())
                .append(fiscalPrinterData.fiscalMemoryNum())
                .append("0",'0',14,true)
                .append(String.valueOf(fiscalPrinterData.lastReceipt()),'0',4,true)
                .append(String.valueOf(fiscalPrinterData.lastInvoiceNum()),'0',10,true)
                .append("11");
        writeToOb(islDataResponseBuilder);
    }
    public void execute(IslOpenReversalReceiptCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        openReceiptAndSetUnp();
        currentFiscalReceipt.openReceipt(command);
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
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



    public void execute(IslItemReversalCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        if(!(commandStack.peek() instanceof IslOpenReversalReceiptCommand) || !(commandStack.peek() instanceof IslItemReversalCommand)) {
            writeToOb(ISLSingleByteResponse.nack());
            return;
        }
        openReceiptAndSetUnp();
        currentFiscalReceipt.addItem(command);
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslItemSaleCommand command) {
        verifyThereIsNoError();
        if(!receiptOpen)
            openReceiptAndSetUnp();
        currentFiscalReceipt.addItem(command);
        commandStack.push(command);
        receiptOpen = true;
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslVoidCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        if(command.getVoidJustLast()) {
            verifyLastCommandWasSaleCommand();
        }
        emptyCommandStack();
        closeReceiptAndClearUnp();
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslValueDiscountOrSurchargeCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        verifyLastCommandWasSaleCommand();
        currentFiscalReceipt.addDiscountOrSurcharge(command);
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }
    public void execute(IslPercentDiscountOrSurcharge command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        try {
            verifyLastCommandWasSaleCommand();
            currentFiscalReceipt.addDiscountOrSurcharge(command);
        }catch (RuntimeException exception) {
            verifyLastCommandWasSubtotal();
        }
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }

    private void verifyLastCommandWasSubtotal() {
        if(!(commandStack.peek() instanceof IslSubtotalCommand))
            throw new RuntimeException("LAST COMMAND WAS NOT SALE OR SUBTOTAL");
    }

    public void execute(IslSubtotalCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        currentFiscalReceipt.subtotal(command);
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslPaymentAndFinishCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        currentFiscalReceipt.addPayment(command);
        commandStack.push(command);
        if(currentFiscalReceipt.isFinished()) {
            fiscalPrinterData.updateTotals(currentFiscalReceipt.paymentsTotals());
            closeReceiptAndClearUnp();
            emptyCommandStack();
            logCurrentPrinterSummedTotals();
        }
        writeToOb(ISLSingleByteResponse.ack());
    }

    private void logCurrentPrinterSummedTotals() {
        logger.info("SUMMED TOTALS: " + summedCurrentPrinterTotals());
    }
    private Long summedCurrentPrinterTotals() {
        return fiscalPrinterData.paymentsTotals()
                .values().stream()
                .reduce(Long::sum)
                .orElse(0L);
    }

    private void emptyCommandStack() {
        while (!commandStack.empty())
            commandStack.pop();
    }

    public void execute(IslDailyReportCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        if(! command.getNoReset()) {
            fiscalPrinterData.clearTotals();
            emptyCommandStack();
        }
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslDepositOrWithdrawMoneyCommand command) {
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
        writeToOb(ISLSingleByteResponse.ack());
    }

    private void verifyTotalIsLessThanAmountForWithdraw(int paymentType,Long amount) {
        if(fiscalPrinterData.paymentsTotals().get(paymentType) < Math.abs(amount))
            throw new RuntimeException("CANT WITHDRAW AS MUCH...");
    }

    public void execute(IslSetDateTimeCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslSetOperatorCommand command) {
        verifyThereIsNoError();
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslPrintCommentCommand command) {
        verifyThereIsNoError();
        if(!receiptOpen) {
            openReceiptAndSetUnp();
        }
        currentFiscalReceipt.addComment(command);
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslKlenReportCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslLastReceiptCopyCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        while(!(commandStack.peek() instanceof IslPaymentAndFinishCommand))
            commandStack.pop();
        Response response = commandStack.isEmpty()
                ? ISLSingleByteResponse.nack()
                : ISLSingleByteResponse.ack();
        writeToOb(response);
    }

    public void execute(IslClearErrorsCommand command) {
        Response response = fiscalPrinterData.attemptToClearErrors()
                ? ISLSingleByteResponse.ack()
                : ISLSingleByteResponse.nack();
        writeToOb(response);
    }

    public void execute(IslGetDateTimeCommand command) {
        LocalDateTime dateTime = LocalDateTime.now();
        logger.info("In GetDateTime Procedure");
        IslDataResponseBuilder builder = new IslDataResponseBuilder();
        builder.append(dateTime.format(
                DateTimeFormatter.ofPattern("ddMMyyHHmmss")
        ));
        writeToOb(builder);
    }


    public void execute(UnIdentifiedCommand command) {
        writeToOb(ISLSingleByteResponse.nack());
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



    private void writeToOb(Response response) {
        this.outputBuffer = response;
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
