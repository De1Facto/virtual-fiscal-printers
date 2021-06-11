package com.viamindsoft.vfp.FiscalPrinters.Ds.supportingDs.isl;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IslBitwiseStatusImplTest {

    @Test
    public void bitwiseStatusTest() {
        IslBitwiseStatus status = IslBitwiseStatusImpl.freshStatus();
        System.out.println(status);
        assertEquals("000000000000",status.toString());
    }

    @Test
    public void testRaisedBit() {
        IslBitwiseStatus status = IslBitwiseStatusImpl.fromStatusArray(
                new byte[][]{
                        new byte[]{ 0x00, 0x01 },
                        new byte[]{ 0x00, 0x08 },
                        new byte[]{ 0x00, 0x00 },
                        new byte[]{ 0x00, 0x00 },
                        new byte[]{ 0x00, 0x00 },
                        new byte[]{ 0x00, 0x00 },
                }
        );
        System.out.println(status);
        assertEquals("010800000000",status.toString());
    }
}