package com.thepriceisright.domain.repository

import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Resource

interface ProductRepository {

    suspend fun lookupBarcode(barcode: String): Resource<Product>

    suspend fun searchProducts(query: String): Resource<List<Product>>
}
