package com.thepriceisright.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BarcodeResultTest {

    @Nested
    @DisplayName("isGS1Compliant()")
    inner class IsGS1Compliant {

        @Test
        fun `valid EAN-13 is compliant`() {
            val result = BarcodeResult("6001069212098", BarcodeFormat.EAN_13, true)
            assertTrue(result.isGS1Compliant())
        }

        @Test
        fun `valid UPC-A is compliant`() {
            val result = BarcodeResult("012345678905", BarcodeFormat.UPC_A, true)
            assertTrue(result.isGS1Compliant())
        }

        @Test
        fun `store-specific barcode starting with 2 is not compliant`() {
            val result = BarcodeResult("2001234567890", BarcodeFormat.EAN_13, true)
            assertFalse(result.isGS1Compliant())
        }

        @Test
        fun `invalid barcode is not compliant`() {
            val result = BarcodeResult("1234567890123", BarcodeFormat.EAN_13, false)
            assertFalse(result.isGS1Compliant())
        }

        @Test
        fun `QR code is not GS1 compliant`() {
            val result = BarcodeResult("some-qr-data", BarcodeFormat.QR_CODE, true)
            assertFalse(result.isGS1Compliant())
        }

        @Test
        fun `wrong length EAN-13 is not compliant`() {
            val result = BarcodeResult("600106921209", BarcodeFormat.EAN_13, true)
            assertFalse(result.isGS1Compliant())
        }
    }

    @Nested
    @DisplayName("isSouthAfrican()")
    inner class IsSouthAfrican {

        @Test
        fun `600 prefix is South African`() {
            val result = BarcodeResult("6001069212098", BarcodeFormat.EAN_13, true)
            assertTrue(result.isSouthAfrican())
        }

        @Test
        fun `601 prefix is South African`() {
            val result = BarcodeResult("6011069212098", BarcodeFormat.EAN_13, true)
            assertTrue(result.isSouthAfrican())
        }

        @Test
        fun `non-SA prefix is not South African`() {
            val result = BarcodeResult("5000159459228", BarcodeFormat.EAN_13, true)
            assertFalse(result.isSouthAfrican())
        }

        @Test
        fun `invalid barcode is not South African`() {
            val result = BarcodeResult("6001069212098", BarcodeFormat.EAN_13, false)
            assertFalse(result.isSouthAfrican())
        }
    }
}
