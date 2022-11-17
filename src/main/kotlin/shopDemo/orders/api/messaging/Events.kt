@file:Suppress("unused")

package shopDemo.orders.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import shopDemo.orders.api.model.OrderCart
import shopDemo.orders.api.model.OrderState
import shopDemo.orders.impl.entity.ItemManagerAggregate
import java.math.BigDecimal
import shopDemo.orders.impl.entity.*
import java.util.*

const val ORDER_CREATED = "ORDER_CREATED_EVENT"
const val ORDER_STATE_CHANGED = "ORDER_STATE_CHANGED_EVENT"

const val DELIVERY_ADDRESS_ADDED = "DELIVERY_ADDRESS_ADDED_EVENT"
const val DELIVERY_DATE_ADDED = "DELIVERY_DATE_ADDED_EVENT"
const val PAYMENT_METHOD_ADDED = "PAYMENT_METHOD_ADDED_EVENT"
const val ORDER_DELETED = "ORDER_DELETED"
const val ITEM_TO_ORDER_ADDED = "ITEM_TO_ORDER_ADDED"
const val ITEM_FROM_ORDER_DELETED = "ITEM_FROM_ORDER_DELETED"
const val SET_AMOUNT_IN_CART = "SET_AMOUNT_IN_CART"
const val CART_ABANDONED_NOTIFY = "CART_ABANDONED_NOTIFY"

const val ITEM_CREATED = "ITEM_CREATED_EVENT"
const val ITEM_DELETED = "ITEM_DELETED_EVENT"
const val ITEM_NAME_UPDATED = "ITEM_NAME_UPDATED_EVENT"
const val ITEM_DESCRIPTION_UPDATED = "ITEM_DESCRIPTION_UPDATED_EVENT"
const val ITEM_PRICE_UPDATED = "ITEM_PRICE_UPDATED_EVENT"
const val ITEM_STOCK_AMOUNT_UPDATED = "ITEM_STOCK_AMOUNT_UPDATED_EVENT"

// -------------------ORDERS-------------------------

@DomainEvent(name = ORDER_CREATED)
class OrderCreatedEvent(
    val orderId: UUID,
    val userId: UUID,
    val orderCart: OrderCart
) : Event<ItemManagerAggregate>(
    name = ORDER_CREATED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = ORDER_STATE_CHANGED)
class OrderStateChangedEvent(
    val orderId: UUID,
    val newState: OrderState
) : Event<ItemManagerAggregate>(
    name = ORDER_STATE_CHANGED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = DELIVERY_DATE_ADDED)
class OrderDeliveryTimeAddedEvent(
    val orderId: UUID,
    val timeSlot: TimeSlot
) : Event<ItemManagerAggregate>(
    name = DELIVERY_DATE_ADDED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = ORDER_DELETED)
class OrderDeletedEvent(
    val orderId: UUID
) : Event<ItemManagerAggregate>(
    name = ORDER_DELETED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = CART_ABANDONED_NOTIFY)
class CartAbandonedNotifyEvent(
    val orderId: UUID
) : Event<ItemManagerAggregate>(
    name = CART_ABANDONED_NOTIFY,
    createdAt = System.currentTimeMillis(),
)
@DomainEvent(name = ITEM_TO_ORDER_ADDED)
class OrderItemAddedEvent(
    val itemId: UUID,
    val orderId: UUID
) : Event<ItemManagerAggregate>(
    name = ITEM_TO_ORDER_ADDED,
    createdAt = System.currentTimeMillis(),
)
@DomainEvent(name = ITEM_FROM_ORDER_DELETED)
class OrderItemDeletedEvent(
    val itemId: UUID,
    val orderId: UUID
) : Event<ItemManagerAggregate>(
    name = ITEM_FROM_ORDER_DELETED,
    createdAt = System.currentTimeMillis(),
)
@DomainEvent(name = SET_AMOUNT_IN_CART)
class OrderItemAmountChangedEvent(
    val itemId: UUID,
    val newAmount: Int,
    val orderId: UUID
) : Event<ItemManagerAggregate>(
    name = SET_AMOUNT_IN_CART,
    createdAt = System.currentTimeMillis(),
)

// -------------------ITEMS-------------------------

@DomainEvent(name = ITEM_CREATED)
class ItemCreatedEvent(
    val itemId: UUID,
    val itemName: String,
    val price: BigDecimal,
    val description: String?,
    val amountInStock: Int,
) : Event<ItemManagerAggregate>(
    name = ITEM_CREATED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = ITEM_DELETED)
class ItemDeletedEvent(
    val itemId: UUID,
) : Event<ItemManagerAggregate>(
    name = ITEM_DELETED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = ITEM_NAME_UPDATED)
class ItemNameUpdatedEvent(
    val itemId: UUID,
    val newName: String,
) : Event<ItemManagerAggregate>(
    name = ITEM_NAME_UPDATED,
    createdAt = System.currentTimeMillis(),
)
@DomainEvent(name = ITEM_DESCRIPTION_UPDATED)
class ItemDescriptionUpdatedEvent(
    val itemId: UUID,
    val newDescription: String?,
) : Event<ItemManagerAggregate>(
    name = ITEM_DESCRIPTION_UPDATED,
    createdAt = System.currentTimeMillis(),
)
@DomainEvent(name = ITEM_PRICE_UPDATED)
class ItemPriceUpdatedEvent(
    val itemId: UUID,
    val newPrice: BigDecimal,
) : Event<ItemManagerAggregate>(
    name = ITEM_PRICE_UPDATED,
    createdAt = System.currentTimeMillis(),
)
@DomainEvent(name = ITEM_STOCK_AMOUNT_UPDATED)
class ItemStockAmountUpdatedEvent(
    val itemId: UUID,
    val newAmount: Int,
) : Event<ItemManagerAggregate>(
    name = ITEM_STOCK_AMOUNT_UPDATED,
    createdAt = System.currentTimeMillis(),
)