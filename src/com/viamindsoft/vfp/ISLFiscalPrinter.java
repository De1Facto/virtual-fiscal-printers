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


    private final IslFiscalPrinterData fiscalPrinterData;
    private boolean receiptOpen = false;
    private final Logger logger;
    private Response outputBuffer;

    private final Stack<Command> commandStack = new Stack<>();
    private IslCurrentFiscalReceipt currentFiscalReceipt = null;
    private IslCurrentFiscalReceipt lastReceipt = null;


    public ISLFiscalPrinter(IslFiscalPrinterData fiscalPrinterData, Logger logger) {
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
        if(!(commandStack.peek() instanceof IslItemSaleCommand) && !(commandStack.peek() instanceof IslItemReversalCommand))
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
            currentFiscalReceipt.addDiscountOrSurcharge(command);
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
        logger.info("Payment Amount Given: "+command.getAmountGiven());
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

    public void execute(IslKlenStatusCommand command) {
        writeToOb(ISLSingleByteResponse.ack());
    }
    public void execute(IslLastReceiptNumberAndSubtotalCommand command) {
        IslDataResponseBuilder builder = new IslDataResponseBuilder();
        builder.append(lastReceipt.documentNumber().toString(),'0',6,true);
        builder.append(lastReceipt.amount().toString(),'0',8,true);
        builder.append(String.valueOf(fiscalPrinterData.lastInvoiceNum()),'0',10,true);
        writeToOb(builder);
    }

    public void execute(IslRevenueByTaxGroupsCommand command) {

    }

    public void execute(IslFMTotalRevenueCommand command) {

    }

    public void execute(IslDiscountsAndSurchargesTotalCommand command) {

    }
    public void execute(IslPrinterVersionCommand command) {
        IslDataResponseBuilder builder = new IslDataResponseBuilder();
        builder.append(fiscalPrinterData.fiscalPrinterModel());
        writeToOb(builder);
    }

    public void execute(IslBatteryStatusCommand command) {

    }

    public void execute(IslCurrentErrorCommand command) {
        IslDataResponseBuilder builder = new IslDataResponseBuilder();
        builder.append(fiscalPrinterData.errorCode());
        writeToOb(builder);
    }

    public void execute(IslCurrentReceiptCurrentTotalCommand command) {

    }

    public void execute(IslBitwiseStatusCommand command) {
        IslDataResponseBuilder builder = new IslDataResponseBuilder();
        builder.append(fiscalPrinterData.getBitwiseStatus().toString());
        writeToOb(builder);
    }

    public void execute(IslReadPaymentAssociationCommand command) {
        IslDataResponseBuilder builder = new IslDataResponseBuilder();
        String payment = fiscalPrinterData.getPaymentsMappings().get(command.getPaymentType()).toString();
        if(payment.length() < 2) {
            payment = "0"+payment;
        }
        builder.append(payment);
        writeToOb(builder);
    }

    public void execute(IslSetPaymentAssociationCommand command) {
        writeToOb(ISLSingleByteResponse.ack()); //NB: This is a stub after all...
    }

    public void execute(IslSetPaymentTextCommand command) {
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(ISLInvoiceReceiverStandardTextFieldCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }

    public void execute(IslReceiverEikAndVatNumbersCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsNotOpen();
        if(!(commandStack.peek() instanceof ISLInvoiceReceiverStandardTextFieldCommand))
            throw new RuntimeException("INVALID INVOICE ORDER");
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
    }

    private void execute(ISLFinishInvoiceCommand command) {
        verifyThereIsNoError();
        verifyReceiptIsOpen();
        commandStack.push(command);
        writeToOb(ISLSingleByteResponse.ack());
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
        lastReceipt = currentFiscalReceipt;
        currentFiscalReceipt = null;
    }



    private void writeToOb(Response response) {
        this.outputBuffer = response;
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
