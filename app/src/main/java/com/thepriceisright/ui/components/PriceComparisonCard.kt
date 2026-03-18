package com.thepriceisright.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.ui.theme.*

@Composable
fun PriceComparisonCard(
    quote: PriceQuote,
    isCheapest: Boolean = false,
    onAddToCart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val retailer = Retailer.fromDisplayName(quote.retailer)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCheapest)
                SavingsGreen.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (isCheapest) 2.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.base)) {
            // Header row: retailer name + cheapest badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RetailerDiamond(
                    retailer = retailer ?: Retailer.CHECKERS,
                    isCheapest = isCheapest
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = quote.retailer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isCheapest) {
                    Surface(
                        color = SavingsGreen,
                        shape = RoundedCornerShape(Radius.full)
                    ) {
                        Text(
                            text = "CHEAPEST",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Price row
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "R${quote.price}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCheapest) SavingsGreen else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = quote.pricePerUnit.formattedPrice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))

                // Stock status
                if (!quote.inStock) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(Radius.sm)
                    ) {
                        Text(
                            text = "Out of stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                        )
                    }
                }
            }

            // Promotion badge
            if (quote.isOnPromotion && quote.promotionDetails != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Surface(
                    color = PromotionPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(Radius.sm)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                    ) {
                        Icon(
                            Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = PromotionPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = quote.promotionDetails,
                            style = MaterialTheme.typography.labelSmall,
                            color = PromotionPurple
                        )
                    }
                }
            }

            // Add to cart button
            if (quote.inStock) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = Spacing.sm)
                ) {
                    Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text("Add to Cart")
                }
            }
        }
    }
}
