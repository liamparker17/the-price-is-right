package com.thepriceisright.domain.usecase

import com.thepriceisright.domain.model.BarcodeResult
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.repository.ProductRepository

class ScanBarcodeUseCase(
    private val productRepository: ProductRepository
) {

    suspend operator fun invoke(barcodeResult: BarcodeResult): Resource<Product> {
        if (!barcodeResult.isValid) {
            return Resource.Error("Invalid barcode scanned")
        }

        if (!barcodeResult.isGS1Compliant()) {
            return Resource.Error(
                "Barcode is not GS1 compliant. Store-specific or internal barcodes are not supported."
            )
        }

        return productRepository.lookupBarcode(barcodeResult.rawValue)
    }
}
