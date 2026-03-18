package com.thepriceisright.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thepriceisright.ui.screens.cart.CartScreen
import com.thepriceisright.ui.screens.home.HomeScreen
import com.thepriceisright.ui.screens.lists.ShoppingListsScreen
import com.thepriceisright.ui.screens.lists.ShoppingListDetailScreen
import com.thepriceisright.ui.screens.loyalty.LoyaltyCardsScreen
import com.thepriceisright.ui.screens.loyalty.AddLoyaltyCardScreen
import com.thepriceisright.ui.screens.more.MoreScreen
import com.thepriceisright.ui.screens.scanner.ScannerScreen
import com.thepriceisright.ui.screens.product.ProductDetailScreen
import com.thepriceisright.ui.screens.settings.SettingsScreen
import com.thepriceisright.ui.screens.alerts.PriceAlertsScreen
import com.thepriceisright.ui.screens.vitality.VitalityDealsScreen
import com.thepriceisright.ui.screens.fuel.FuelCalculatorScreen

@Composable
fun GrocifyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { barcode ->
                    navController.navigate("product/$barcode")
                }
            )
        }
        composable(Screen.Scanner.route) {
            ScannerScreen(
                onBarcodeScanned = { barcode ->
                    navController.navigate("product/$barcode")
                }
            )
        }
        composable(Screen.Cart.route) {
            CartScreen()
        }
        composable(Screen.Lists.route) {
            ShoppingListsScreen(
                onListClick = { listId ->
                    navController.navigate("list/$listId")
                }
            )
        }
        composable(Screen.More.route) {
            MoreScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
            ProductDetailScreen(
                barcode = barcode,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LoyaltyCards.route) {
            LoyaltyCardsScreen(
                onAddCard = { navController.navigate(Screen.AddLoyaltyCard.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddLoyaltyCard.route) {
            AddLoyaltyCardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PriceAlerts.route) {
            PriceAlertsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.VitalityDeals.route) {
            VitalityDealsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.FuelCalculator.route) {
            FuelCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ShoppingListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            ShoppingListDetailScreen(
                listId = listId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
