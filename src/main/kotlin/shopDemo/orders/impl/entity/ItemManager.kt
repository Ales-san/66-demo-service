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
    private var orders: MutableMap<UUID, Order> = mutableMapOf()
    private var items: MutableMap<UUID, Item> = mutableMapOf()
    private var updatedTime: Long //= System.currentTimeMillis()

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
    fun setOrderDeliveryTime(timeSlot: TimeSlot, orderId: UUID): OrderDeliveryTimeAddedEvent {
        if (orders[orderId]?.state != OrderState.InProcess)
            throw IllegalStateException("Order is ${orders[orderId]?.state}")

        return OrderDeliveryTimeAddedEvent(orderId, timeSlot)
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

    fun deleteOrder(orderId: UUID): OrderDeletedEvent {
        if (!orders.containsKey(orderId))
            throw ObjectAlreadyExistException("Can't delete order with id $orderId, because it doesn't exist")

        return OrderDeletedEvent(orderId)
    }

    fun checkOrderStateCollected(OrderState state) {
        if (state != OrderState.Collected)
            throw IllegalStateException("Order is not in state ${orders[orderId]?.state}")
    }
    fun notifyAbandonedCart(time: Int, orderId: UUID): CartAbandonedNotifyEvent {
        checkOrderStateCollected(orders[orderId]?.state)

        // updateTime
        if (orders[orderId]?.state != OrderState.Collected)
            throw IllegalStateException("Can't notify about abandoned cart, because order is not in state ${orders[orderId]?.state}")

        return CartAbandonedNotifyEvent(orderId)
    }

    fun addItemToOrder(itemId: UUID, orderId: UUID): OrderItemAddedEvent {
        checkOrderStateCollected(orders[orderId]?.state)
        return OrderItemAddedEvent(itemId, orderId)
    }

    fun deleteItemFromOrder(itemId: UUID, orderId: UUID): OrderItemDeletedEvent {
        checkOrderStateCollected(orders[orderId]?.state)

        if (orders[orderId]?.orderRows?.containsKey(itemId) != true)
            throw IllegalStateException("Can't delete item with id $itemId from order with id $orderId, because item is not in the cart")

        return OrderItemDeletedEvent(itemId, orderId)
    }

    fun setAmountInCart(itemId: UUID, newAmount: Int, orderId: UUID): OrderItemAmountChangedEvent {
        checkOrderStateCollected(orders[orderId]?.state)
        return OrderItemAmountChangedEvent(itemId, newAmount, orderId)
    }

    @StateTransitionFunc
    fun createNewOrder(orderCreatedEvent: OrderCreatedEvent) {
        with(orderCreatedEvent) {
            orders.put(orderId, Order(orderId, userId, createdAt, orderRows))
        }
    }

    @StateTransitionFunc
    fun setOrderDeliveryTime(timeSlot: TimeSlot, orderId: UUID) {
        with(OrderDeliveryTimeAddedEvent) {
            orders.put(orderId, Order(orderId, userId, createdAt, orderRows))
        }
        deliveryDate = deliveryDateAddedEvent.date
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

class Order(_orderId: UUID, _userId: UUID, _creationTime: Long, _orderRows: MutableMap<UUID, OrderRow>) {
    var orderId: UUID = _orderId
    var userId: UUID = _userId
    var creationTime: Long = _creationTime // System.currentTimeMillis()
    var state: OrderState? = null
    var deliveryDate: TimeSlot? = null
    var orderRows: MutableMap<UUID, OrderRow> = _orderRows

    fun getId(): UUID = orderId
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



