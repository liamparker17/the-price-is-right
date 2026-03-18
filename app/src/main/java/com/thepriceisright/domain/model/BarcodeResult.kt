package com.thepriceisright.domain.model

data class BarcodeResult(
    val rawValue: String,
    val format: BarcodeFormat,
    val isValid: Boolean
) {

    fun isGS1Compliant(): Boolean {
        if (!isValid) return false

        // Internal/store-specific barcodes starting with 2 are not GS1 compliant
        if (rawValue.startsWith("2")) return false

        // Validate length based on format
        val hasValidLength = when (format) {
            BarcodeFormat.EAN_13 -> rawValue.length == 13
            BarcodeFormat.EAN_8 -> rawValue.length == 8
            BarcodeFormat.UPC_A -> rawValue.length == 12
            BarcodeFormat.UPC_E -> rawValue.length == 8
            BarcodeFormat.QR_CODE -> false
            BarcodeFormat.UNKNOWN -> false
        }

        return hasValidLength
    }

    fun isSouthAfrican(): Boolean {
        // SA GS1 prefix is 600 or 601
        return isGS1Compliant() && (rawValue.startsWith("600") || rawValue.startsWith("601"))
    }
}

enum class BarcodeFormat {
    UPC_A,
    UPC_E,
    EAN_8,
    EAN_13,
    QR_CODE,
    UNKNOWN
}
