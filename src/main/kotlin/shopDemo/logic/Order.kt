@file:Suppress("unused")

package shopDemo.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import shopDemo.api.*
import java.util.*

class Order : AggregateState<UUID, OrderAggregate> {
    private lateinit var orderId: UUID
    private lateinit var userId: UUID
    private var creationTime: Long = System.currentTimeMillis()
    private var state: OrderState? = null

    private var deliveryAddress: String? = null
    private var paymentMethod: PaymentMethod? = null
    private var deliveryDate: Date? = null

    private var orderRows: Map<UUID, OrderRow> = mapOf()

    override fun getId(): UUID = orderId

    fun createNewOrder(id: UUID = UUID.randomUUID(), userId: UUID, products: Map<UUID, Int>): OrderCreatedEvent {
        if (state != null)
            throw IllegalStateException("Order is $state")

        if (products.isEmpty())
            throw IllegalArgumentException("Can't create empty order")

        return OrderCreatedEvent(id, userId,
            products.map { (productId, count) ->
                OrderRow(UUID.randomUUID(), productId, count)
            }
        )
    }

    fun addDeliveryAddress(address: String): DeliveryAddressAddedEvent {
        if (state != OrderState.InProcess)
            throw IllegalStateException("Order is $state")

        return DeliveryAddressAddedEvent(orderId, address)
    }

    fun addDeliveryDate(date: Date): DeliveryDateAddedEvent {
        if (state != OrderState.InProcess)
            throw IllegalStateException("Order is $state")

        return DeliveryDateAddedEvent(orderId, date)
    }

    fun addPaymentMethod(paymentMethod: PaymentMethod): PaymentMethodAddedEvent {
        if (state != OrderState.InProcess)
            throw IllegalStateException("Order is $state")

        return PaymentMethodAddedEvent(orderId, paymentMethod)
    }

    private fun getPossibleStates() = when (state) {
        null -> listOf(OrderState.InProcess)
        OrderState.InProcess -> listOf(OrderState.Processed, OrderState.Discarded)
        OrderState.Processed -> listOf(OrderState.Paid, OrderState.Discarded)
        OrderState.Paid -> listOf(OrderState.Delivered)
        OrderState.Delivered -> listOf()
        OrderState.Discarded -> listOf()
    }

    fun changeOrderState(newState: OrderState): OrderStateChangedEvent {
        if (newState !in getPossibleStates())
            throw IllegalStateException("Can't change state from $state to $newState")

        if (newState == OrderState.Processed) {
            deliveryAddress ?: throw IllegalStateException("Can't process order without address")
            deliveryDate ?: throw IllegalStateException("Can't process order without date")
            paymentMethod ?: throw IllegalStateException("Can't process order without payment method")
        }

        return OrderStateChangedEvent(orderId, newState)
    }

    @StateTransitionFunc
    fun createNewOrder(orderCreatedEvent: OrderCreatedEvent) {
        orderId = orderCreatedEvent.orderId
        userId = orderCreatedEvent.userId
        creationTime = orderCreatedEvent.createdAt
        state = OrderState.InProcess
        orderRows = orderCreatedEvent.orderRows.associateBy { it.id }
    }

    @StateTransitionFunc
    fun addDeliveryAddress(deliveryAddressAddedEvent: DeliveryAddressAddedEvent) {
        deliveryAddress = deliveryAddressAddedEvent.address
    }

    @StateTransitionFunc
    fun addDeliveryDate(deliveryDateAddedEvent: DeliveryDateAddedEvent) {
        deliveryDate = deliveryDateAddedEvent.date
    }

    @StateTransitionFunc
    fun addPaymentMethod(paymentMethodAddedEvent: PaymentMethodAddedEvent) {
        paymentMethod = paymentMethodAddedEvent.paymentMethod
    }

    @StateTransitionFunc
    fun changeOrderState(orderStateChangedEvent: OrderStateChangedEvent) {
        state = orderStateChangedEvent.newState
    }

    override fun toString(): String {
        return "Order(\n" +
                " orderId=$orderId,\n" +
                " userId=$userId,\n" +
                " creationTime=$creationTime,\n" +
                " state=$state,\n" +
                " deliveryAddress=$deliveryAddress,\n" +
                " paymentMethod=$paymentMethod,\n" +
                " deliveryDate=$deliveryDate,\n" +
                " orderRows=[\n  ${orderRows.entries.joinToString("\n  ") { it.value.toString() }}\n ]\n" +
                ")"
    }


}

enum class OrderState {
    InProcess,
    Processed,
    Paid,
    Delivered,
    Discarded
}

enum class PaymentMethod {
    Cash,
    Card
}

data class OrderRow(
    val id: UUID,
    val productId: UUID,
    val productCount: Int,
)

