package com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class IslCurrentFiscalReceiptImpl implements IslCurrentFiscalReceipt {
    private final Long documentNumber;
    private String uniqueSaleNumber = "";
    private Long amount = 0L;
    private Boolean isReversal = false;
    private final Map<Integer,Long> paymentsTotals = new HashMap<>();
    private final Stack<Command> commandStack = new Stack<>();
    private Boolean isFinished = false;

    public IslCurrentFiscalReceiptImpl(Long documentNumber) {
        this.documentNumber = documentNumber;
        initializePaymentsMap();
    }

    private void initializePaymentsMap() {
        for(var i=0; i < 10; i++) {
            paymentsTotals.put(i,0L);
        }
    }

    @Override
    public Long documentNumber() {
        return documentNumber;
    }

    @Override
    public String uniqueSaleNumber() {
        return uniqueSaleNumber;
    }

    @Override
    public Long amount() {
        return amount;
    }

    @Override
    public Map<Integer, Long> paymentsTotals() {
        return paymentsTotals;
    }

    @Override
    public Boolean isReversal() {
        return isReversal;
    }

    @Override
    public Boolean isFinished() {
        return isFinished;
    }

    @Override
    public void openReceipt(Command openReceiptCommand) {
        if(!(openReceiptCommand instanceof IslOpenReversalReceiptCommand))
            throw new RuntimeException("INVALID OPEN RECEIPT COMMAND");
        IslOpenReversalReceiptCommand command = (IslOpenReversalReceiptCommand) openReceiptCommand;
        uniqueSaleNumber = command.getUniqueSaleNumber();
        isReversal = true;
        commandStack.push(command);
    }

    @Override
    public void addPayment(Command addPaymentCommand) {
        throw new RuntimeException("INVALID COMMAND");
    }

    @Override
    public void addItem(Command addItemCommand) {
        throw new RuntimeException("Invalid COMMAND");
    }

    @Override
    public void addDiscountOrSurcharge(Command addDiscountOrSurcharge) {
        throw new RuntimeException("INVALID COMMAND FOR DISCOUNT");
    }

    @Override
    public void attemptVoid(Command voidCommand) {
        throw new RuntimeException("INVALID COMMAND");
    }

    @Override
    public void subtotal(Command subtotal) {
        throw new RuntimeException("INVALID COMMAND");
    }

    @Override
    public void addComment(Command addComment) {
        throw new RuntimeException("INVALID COMMAND FOR COMMENT");
    }

    @Override
    public void addItem(IslItemSaleCommand addItemCommand) {
        if(addItemCommand instanceof IslItemReversalCommand) {
            addItem((IslItemReversalCommand) addItemCommand);
            return;
        }
        if(!uniqueSaleNumber.equals(addItemCommand.getUniqueSaleNumber()) && !uniqueSaleNumber.equals("")) {
            throw new RuntimeException("INVALID UNIQUE SALE NUMBER");
        }
        uniqueSaleNumber = addItemCommand.getUniqueSaleNumber();
        amount += addItemCommand.getPrice() * Math.round(addItemCommand.getQuantity() / (float)1000);
        commandStack.push(addItemCommand);
    }

    public void addItem(IslItemReversalCommand addReversalItem) {
        if(!isReversal)
            throw new RuntimeException("CANT DO REVERSAL OUTSIDE REVERSAL RECEIPT");
        if(!addReversalItem.getUniqueSaleNumber().equals(uniqueSaleNumber))
            throw new RuntimeException("INVALID UNIQUE SALE NUM");
        amount += addReversalItem.getPrice() * Math.round(addReversalItem.getQuantity() / (float) 1000);
        commandStack.push(addReversalItem);
    }

    @Override
    public void addPayment(IslPaymentAndFinishCommand addPaymentCommand) {
        if(addPaymentCommand.getAmountGiven().equals(0L)) {
            paymentsTotals.put(addPaymentCommand.getPayment().intValue(),amount);
            finishReceiptSequence();
            return;
        }
        paymentsTotals.put(addPaymentCommand.getPayment().intValue(),addPaymentCommand.getAmountGiven());
        commandStack.push(addPaymentCommand);
        if(!addPaymentCommand.getFlag()) {
            paymentsTotals.put(
                    addPaymentCommand.getPayment().intValue(),
                    paymentsTotals.get(addPaymentCommand.getPayment().intValue()) + (amount - computeReceiptTotalPayments())
                    );
            finishReceiptSequence();
            return;
        }
        amount -= addPaymentCommand.getAmountGiven();
    }

    private void finishReceiptSequence() {
        amount = 0L;
        isFinished = true;
    }

    private Long computeReceiptTotalPayments() {
        return paymentsTotals.values().stream().reduce(0L, Long::sum);
    }

    @Override
    public void attemptVoid(IslVoidCommand voidCommand) {
        if(voidCommand.getVoidJustLast()) {
            voidLastOnly();
            commandStack.push(voidCommand);
            return;
        }
        voidEverything(voidCommand);
    }

    private void voidLastOnly() {
        if(isNotSaleCommand(commandStack.peek()) && !isDiscountOrSurcharge(commandStack.peek())) {
            throw new RuntimeException("CAN't VOID ...");
        }
        Command lastCommand = commandStack.pop();
        if(!isNotSaleCommand(lastCommand)) {
            amount -= ((IslItemSaleCommand) commandStack.peek()).getPrice() * ((IslItemSaleCommand) commandStack.peek()).getQuantity();
            return;
        }
        Command secondToLastCommand = commandStack.peek();
        amount -= ((IslItemSaleCommand) commandStack.peek()).getPrice() * ((IslItemSaleCommand) commandStack.peek()).getQuantity();
        if(lastCommand instanceof IslValueDiscountOrSurchargeCommand) {
            reduceValueDiscountOrSurcharge((IslValueDiscountOrSurchargeCommand) lastCommand);
            return;
        }
        reducePercentDiscountOrSurcharge((IslPercentDiscountOrSurcharge) lastCommand,(IslItemSaleCommand) secondToLastCommand);
        commandStack.push(lastCommand);
    }
    private void reduceValueDiscountOrSurcharge(IslValueDiscountOrSurchargeCommand command) {
        Long value = command.getAmount();
        if(!command.getDiscount()) {
            value = -value;
        }
        amount += value;
    }

    private void reducePercentDiscountOrSurcharge(IslPercentDiscountOrSurcharge percentDiscountOrSurcharge, IslItemSaleCommand saleCommand) {
        Long saleAmount = saleCommand.getPrice() * saleCommand.getQuantity();
        long discountAmount = Math.round(percentDiscountOrSurcharge.getAmount() * saleAmount / (double)100);
        if(!percentDiscountOrSurcharge.getDiscount()) {
            discountAmount = - discountAmount;
        }
        amount += discountAmount;
    }

    private void voidEverything(IslVoidCommand command) {
        amount = 0L;
        commandStack.push(command);
    }

    @Override
    public void addDiscountOrSurcharge(IslPercentDiscountOrSurcharge addPercentDiscountOrSurcharge) {
        if(isNotSaleCommand(commandStack.peek()) && !(commandStack.peek() instanceof IslSubtotalCommand))
            throw new RuntimeException("CANT ADD DISCOUNT OR SURCHARGE");
        if(commandStack.peek() instanceof IslSubtotalCommand) {
            percentDiscountOrSurchargeToSubtotal(addPercentDiscountOrSurcharge);
            return;
        }
        percentDiscountOrSurchargeToLast(addPercentDiscountOrSurcharge);
    }
    private void percentDiscountOrSurchargeToSubtotal(IslPercentDiscountOrSurcharge command) {
        long value = Math.round(amount * (command.getAmount() / (double)100));
        if(command.getDiscount()) {
            value = -value;
        }
        amount += value;
        commandStack.push(command);
    }

    private void percentDiscountOrSurchargeToLast(IslPercentDiscountOrSurcharge command) {
        IslItemSaleCommand lastSale = (IslItemSaleCommand) commandStack.peek();
        long value = Math.round(lastSale.getPrice()*lastSale.getQuantity() * (command.getAmount() / (double) 100));
        if(command.getDiscount()) {
            value = -value;
        }
        amount += value;
        commandStack.push(command);
    }

    @Override
    public void addDiscountOrSurcharge(IslValueDiscountOrSurchargeCommand addValueDiscountOrSurcharge) {
        if(isNotSaleCommand(commandStack.peek()))
            throw new RuntimeException("CANT ADD DISCOUNT OR SURCHARGE");
        Long value = addValueDiscountOrSurcharge.getAmount();
        if(addValueDiscountOrSurcharge.getDiscount()) {
            value = -value;
        }
        amount += value;
        commandStack.push(addValueDiscountOrSurcharge);
    }

    @Override
    public void subtotal(IslSubtotalCommand command) {
        if(amount == 0) return;
        commandStack.push(command);
    }


    @Override
    public void addComment(IslPrintCommentCommand commentCommand) {
        if(!uniqueSaleNumber.equals(commentCommand.getUniqueSaleNumber()) && !uniqueSaleNumber.equals("")) {
            throw new RuntimeException("INVALID UNIQUE SALE NUMBER");
        }
        uniqueSaleNumber = commentCommand.getUniqueSaleNumber();
        commandStack.push(commentCommand);
    }


    private boolean isNotSaleCommand(Command command) {
        return !(command instanceof IslItemSaleCommand);
    }
    private boolean isDiscountOrSurcharge(Command command) {
        return command instanceof IslPercentDiscountOrSurcharge
                || command instanceof IslValueDiscountOrSurchargeCommand;
    }
}
