package com.thepriceisright.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.ui.theme.*

@Composable
fun RetailerDiamond(
    retailer: Retailer,
    isCheapest: Boolean = false,
    isAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    val color = when (retailer) {
        Retailer.CHECKERS -> CheckersRed
        Retailer.PICK_N_PAY -> PnPBlue
        Retailer.WOOLWORTHS -> WoolworthsBlack
        Retailer.SPAR -> SparGreen
        Retailer.SHOPRITE -> ShopriteRed
    }

    val displayColor = if (isAvailable) color else color.copy(alpha = 0.25f)
    val borderColor = if (isCheapest) CheapestGold else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(size.width / 2, size.height)
                lineTo(0f, size.height / 2)
                close()
            }
            if (isCheapest) {
                drawPath(path, borderColor)
                val innerPath = Path().apply {
                    val inset = 3f
                    moveTo(size.width / 2, inset)
                    lineTo(size.width - inset, size.height / 2)
                    lineTo(size.width / 2, size.height - inset)
                    lineTo(inset, size.height / 2)
                    close()
                }
                drawPath(innerPath, displayColor)
            } else {
                drawPath(path, displayColor)
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = retailer.displayName.take(3),
            style = MaterialTheme.typography.labelSmall,
            color = if (isAvailable) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RetailerDiamondRow(
    availableRetailers: Set<Retailer>,
    cheapestRetailer: Retailer? = null,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        Retailer.entries.forEach { retailer ->
            RetailerDiamond(
                retailer = retailer,
                isCheapest = retailer == cheapestRetailer,
                isAvailable = retailer in availableRetailers
            )
        }
    }
}
