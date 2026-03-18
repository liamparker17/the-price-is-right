# Grocify - SA Grocery Price Comparison App

## Project Overview
Android app (Kotlin) for comparing grocery prices across 5 South African retailers: **Checkers, Pick n Pay, Woolworths, SPAR, Shoprite**. Currency: ZAR. No accounts/login. Privacy-first (all data on-device).

## Tech Stack
- **UI:** Jetpack Compose + Material 3 (dark mode support)
- **Architecture:** MVVM + Clean Architecture (domain/data/ui layers)
- **DI:** Hilt (Dagger)
- **Scanner:** CameraX + Google ML Kit Barcode
- **Network:** Retrofit + OkHttp + Moshi
- **Local Storage:** DataStore Preferences
- **Async:** Kotlin Coroutines + StateFlow
- **Testing:** JUnit 5 + MockK + Turbine

## Package Structure
```
com.thepriceisright/
├── domain/
│   ├── model/        # Product, PriceQuote, PricePerUnit, Retailer, CartItem,
│   │                 # SmartCart, ShoppingList, LoyaltyCard, FuelConfig,
│   │                 # PriceAlert, VitalityDeal, UserPreferences, Resource,
│   │                 # BarcodeResult
│   ├── repository/   # Interfaces: Product, Price, Cart, ShoppingList,
│   │                 # LoyaltyCard, PriceAlert, UserPreferences
│   └── usecase/      # ScanBarcode, ComparePrices, CalculateSmartCart
├── data/
│   ├── remote/
│   │   ├── api/          # OpenFoodFactsApi, UpcItemDbApi, IgrosaApi
│   │   ├── interceptor/  # ApiLoggingInterceptor
│   │   ├── source/       # RetailerSource interface + 5 implementations
│   │   └── strategy/     # PricingStrategy, StrategyChain, IgrosaStrategy,
│   │                     # RetailerApiStrategy, WebScrapingStrategy, CachedPricingStrategy
│   ├── local/            # DataStore classes (Cart, ShoppingList, LoyaltyCard, etc.)
│   ├── repository/       # All repository implementations
│   └── service/          # PriceComparisonService (orchestrator)
├── di/               # Hilt modules: Network, Repository, Retailer, Data, App
├── scanner/          # BarcodeAnalyzer, CameraManager, LoyaltyCardScanner
└── ui/
    ├── theme/         # Color, Type, Theme (dark/light), Spacing tokens
    ├── navigation/    # Screen routes, GrocifyNavHost, bottom nav
    ├── components/    # RetailerDiamond, PriceComparisonCard, SearchBar, LoadingStates
    └── screens/       # home, scanner, cart, lists, product, loyalty, settings,
                       # alerts, vitality, fuel, more
```

## Multi-Pronged Pricing Strategy (NO MOCK DATA)
Each retailer source uses a `StrategyChain` that tries real approaches in priority order.
If ALL strategies fail, the app shows "Price data unavailable" — never fake prices.
```
Priority 1: iGrosa API (RapidAPI) — Checkers, Shoprite, PnP only
Priority 2: RetailerApiStrategy — reverse-engineered endpoints (all 5)
             Checkers/Shoprite: /medusa/v2/ (SAP, via shoprite-miner)
             PnP: /pnpstorefront/ (SAP Hybris)
             Woolworths: commercetools GraphQL
             SPAR: SPAR2U API
Priority 3: WebScrapingStrategy — JSoup with per-retailer CSS selectors
```
- `CachedPricingStrategy` wraps any strategy with 4-hour DataStore TTL
- API key: Set `igrosa_api_key` in `local.properties` → BuildConfig.IGROSA_API_KEY
- Endpoints WILL break — update URLs in `RetailerApiStrategy.ENDPOINTS`
  and selectors in `WebScrapingStrategy.SELECTORS`

## Conventions
- Use `@Inject constructor` for Hilt injection
- All repository operations return `Resource<T>` or `Flow`
- Prices in BigDecimal, currency defaults to "ZAR"
- No external accounts/tracking — everything stored locally via DataStore
- No mock/fake data anywhere — show "unavailable" when data can't be fetched

## Launch Readiness Assessment

### READY (code complete)
- [x] Full MVVM + Clean Architecture (domain/data/ui)
- [x] All 11 Compose screens with Material 3
- [x] 5-state UX: idle, loading (shimmer), success, error (retry), empty (CTA)
- [x] Dark mode (system + manual toggle)
- [x] CameraX barcode scanner with ML Kit
- [x] Smart cart optimization (single-store + mixed-basket)
- [x] Shopping lists with share codes
- [x] Loyalty card wallet
- [x] Fuel cost calculator
- [x] Price drop alerts (local tracking)
- [x] Multi-pronged pricing strategy (iGrosa + API + scraping)
- [x] Hilt DI wired end-to-end
- [x] Unit tests (PricePerUnit, Barcode, Fuel, 2 ViewModels)

### BLOCKING for launch
- [ ] **iGrosa API key** — sign up at RapidAPI, add to local.properties
- [ ] **Verify retailer API endpoints** — inspect Checkers/PnP/Woolworths/SPAR/Shoprite
      app traffic with mitmproxy or browser DevTools, update URLs in RetailerApiStrategy
- [ ] **Verify JSoup CSS selectors** — load each retailer search page, confirm selectors
      in WebScrapingStrategy match current site structure
- [ ] **Compile & fix build errors** — code was generated without compilation;
      expect import issues, missing type adapters, API mismatches
- [ ] **ProGuard/R8 rules** — create proguard-rules.pro for Retrofit, Moshi, JSoup
- [ ] **App icon & branding assets** — launcher icon, splash screen, retailer logos

### SHOULD HAVE for launch
- [ ] Push notifications (price alerts, loyalty card reminders)
- [ ] Geofencing for loyalty card proximity alerts
- [ ] PDF export for shopping lists
- [ ] Product image loading via Coil
- [ ] Live exchange rate API for imported products
- [ ] Proper error analytics (Crashlytics or similar)
- [ ] Play Store listing assets (screenshots, description, privacy policy)

### NICE TO HAVE (post-launch)
- [ ] Drag & drop reorder in shopping lists
- [ ] Price history charts
- [ ] Vitality deals real data integration
- [ ] Widget for tracked product prices
- [ ] Offline mode with fuller caching
