@file:Suppress("unused")

package shopDemo.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import shopDemo.logic.OrderRow
import shopDemo.logic.OrderState
import shopDemo.logic.PaymentMethod
import java.util.*

const val ORDER_CREATED = "ORDER_CREATED_EVENT"
const val ORDER_STATE_CHANGED = "ORDER_STATE_CHANGED_EVENT"

const val DELIVERY_ADDRESS_ADDED = "DELIVERY_ADDRESS_ADDED_EVENT"
const val DELIVERY_DATE_ADDED = "DELIVERY_DATE_ADDED_EVENT"
const val PAYMENT_METHOD_ADDED = "PAYMENT_METHOD_ADDED_EVENT"

@DomainEvent(name = ORDER_CREATED)
class OrderCreatedEvent(
    val orderId: UUID,
    val userId: UUID,
    val orderRows: List<OrderRow>
) : Event<OrderAggregate>(
    name = ORDER_CREATED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = ORDER_STATE_CHANGED)
class OrderStateChangedEvent(
    val orderId: UUID,
    val newState: OrderState
) : Event<OrderAggregate>(
    name = ORDER_STATE_CHANGED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = DELIVERY_ADDRESS_ADDED)
class DeliveryAddressAddedEvent(
    val orderId: UUID,
    val address: String
) : Event<OrderAggregate>(
    name = DELIVERY_ADDRESS_ADDED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = DELIVERY_DATE_ADDED)
class DeliveryDateAddedEvent(
    val orderId: UUID,
    val date: Date
) : Event<OrderAggregate>(
    name = DELIVERY_DATE_ADDED,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = PAYMENT_METHOD_ADDED)
class PaymentMethodAddedEvent(
    val orderId: UUID,
    val paymentMethod: PaymentMethod
) : Event<OrderAggregate>(
    name = PAYMENT_METHOD_ADDED,
    createdAt = System.currentTimeMillis(),
)