package com.viamindsoft.vfp.FiscalPrinters.Ds.supportingDs.isl;

public class IslBitwiseStatusImpl implements IslBitwiseStatus {
    private byte[][] status = new byte[][]{
            new byte[]{ 0x00, 0x00 },
            new byte[]{ 0x00, 0x00 },
            new byte[]{ 0x00, 0x00 },
            new byte[]{ 0x00, 0x00 },
            new byte[]{ 0x00, 0x00 },
            new byte[]{ 0x00, 0x00 },
    };

    private IslBitwiseStatusImpl() {

    }
    private IslBitwiseStatusImpl(byte[][] status) {
        this.status = status;
    }

    public static IslBitwiseStatusImpl freshStatus() {
        return new IslBitwiseStatusImpl();
    }
    public static IslBitwiseStatusImpl fromStatusArray(byte[][] status) {
        return new IslBitwiseStatusImpl(status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte[] statusByte: status) {
            for (byte b: statusByte) {
                sb.append(b);
            }
        }
        return sb.toString();
    }
}
