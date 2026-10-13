package com.example.data.model

enum class UserPersona(
    val title: String,
    val subtitle: String,
    val primaryCategory: String,
    val iconEmoji: String
) {
    INDIAN_ETHNIC(
        title = "Indian Ethnic Wear",
        subtitle = "Silk sarees, designer kurtas, festive jewelry and apparel",
        primaryCategory = "Fashion",
        iconEmoji = "🥻"
    ),
    KITCHEN_PANTRY(
        title = "Indian Groceries",
        subtitle = "Royal basmati rice, pure desi ghee, spices, and lentils",
        primaryCategory = "Groceries",
        iconEmoji = "🌾"
    ),
    GADGETS_MOBILES(
        title = "Mobiles & Laptops",
        subtitle = "Flagship 5G phones, thin ultrabooks, and tech gear",
        primaryCategory = "Tech",
        iconEmoji = "📱"
    ),
    SMART_LIVING(
        title = "Smart Living",
        subtitle = "Home automation, cozy ambiance, specialty coffee",
        primaryCategory = "Living",
        iconEmoji = "🏡"
    ),
    ACTIVE_WELLNESS(
        title = "Active Wellness",
        subtitle = "Recovery gear, workout endurance, healthy lifestyle",
        primaryCategory = "Fitness",
        iconEmoji = "⚡"
    )
}

data class TastePreference(
    val selectedPersona: UserPersona = UserPersona.INDIAN_ETHNIC,
    val budgetFriendlyOnly: Boolean = false,
    val fastDeliveryOnly: Boolean = false,
    val highRatedOnly: Boolean = false,
    val favoriteCategories: Set<String> = setOf("Fashion", "Groceries", "Tech")
)
