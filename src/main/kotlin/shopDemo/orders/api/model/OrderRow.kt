package shopDemo.orders.api.model

import java.util.*

data class OrderRow(
    val id: UUID,
    val productId: UUID,
    val productCount: Int,
)