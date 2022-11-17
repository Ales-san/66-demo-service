@file:Suppress("unused")

package shopDemo.orders.impl.entity

import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.common.exception.ObjectAlreadyExistException
import com.itmo.microservices.demo.common.exception.WrongValueException
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import shopDemo.orders.api.*
import shopDemo.orders.api.model.OrderRow
import shopDemo.orders.api.model.OrderState
import java.math.BigDecimal
import java.util.*

class ItemManager : AggregateState<UUID, ItemManagerAggregate> {
    private lateinit var itemManagerId: UUID
    private lateinit var orders: Map<UUID, Order>
    private lateinit var items: Map<UUID, Item>
    private val creationTime = System.currentTimeMillis()

    override fun getId(): UUID = itemManagerId

    fun createNewOrder(userId: UUID, cart: Map<UUID, Int>, orderId: UUID = UUID.randomUUID()): OrderCreatedEvent {
        //if user exists
        if (orders.containsKey(orderId))
            throw ObjectAlreadyExistException("Order with this id already exists")

//        if (curOrder.state != null)
//            throw IllegalStateException("Order is $curOrder.state")

        if (cart.isEmpty())
            throw IllegalArgumentException("Can't create empty order")

        for ((itemId, itemAmount) in cart) {
            if (itemAmount < 0) throw WrongValueException("Item amount in order must not be negative!")
            if (!items.containsKey(itemId)) throw NotFoundException("No item with such id $itemId")
        }

        return OrderCreatedEvent(orderId, userId,
            cart.map { (productId, count) ->
                OrderRow(UUID.randomUUID(), productId, count)
            }
        )
    }

    fun addDeliveryAddress(orderId: UUID, address: String): DeliveryAddressAddedEvent {
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

class Order(_orderId: UUID, _userId: UUID) {
    var orderId: UUID = _orderId
    var userId: UUID = _userId
    var creationTime: Long = System.currentTimeMillis()
    var state: OrderState? = null

    var deliveryAddress: String? = null
    var paymentMethod: PaymentMethod? = null
    var deliveryDate: Date? = null

    var orderRows: Map<UUID, OrderRow> = mapOf()

//    fun getId(): UUID = orderId
}

class Item(_itemId: UUID, _name: String, _price: BigDecimal, _description: String? = null, _amountInStock: Int = 0) {
    var itemId: UUID = _itemId
    val creationTime: Long = System.currentTimeMillis()
    var name: String = _name

    var price: BigDecimal = _price
        set(value) {
            if (value > BigDecimal.ZERO) field = value else throw WrongValueException("Price must not be negative!")
        }
    var description: String? = _description
    var amountInStock: Int = _amountInStock
        set(value) {
            if (value >= 0) field = value else throw WrongValueException("Amount in stock must not be negative!")
        }

//    fun getId(): UUID = orderId
}

enum class PaymentMethod {
    Cash,
    Card
}



