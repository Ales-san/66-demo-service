@file:Suppress("unused")

package shopDemo.orders.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import shopDemo.orders.api.model.OrderRow
import shopDemo.orders.api.model.OrderState
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
@DomainEvent(name = ORDER_CREATED)
class OrderCreatedEvent(
    val orderId: UUID,
    val userId: UUID,
    val orderRows: List<OrderRow>
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


