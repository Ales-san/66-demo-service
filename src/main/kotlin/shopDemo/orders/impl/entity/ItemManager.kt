@file:Suppress("unused")

package shopDemo.orders.impl.entity

import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.common.exception.ObjectAlreadyExistException
import com.itmo.microservices.demo.common.exception.WrongValueException
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import shopDemo.orders.api.*
import shopDemo.orders.api.model.OrderCart
import shopDemo.orders.api.model.OrderState
import java.math.BigDecimal
import java.util.*
import kotlin.properties.Delegates

class ItemManager : AggregateState<UUID, ItemManagerAggregate> {
    lateinit var itemManagerId: UUID
    internal var orders: MutableMap<UUID, Order> = mutableMapOf()
    internal var items: MutableMap<UUID, Item> = mutableMapOf()
    var updatedTime by Delegates.notNull<Long>() //= System.currentTimeMillis()

    override fun getId(): UUID = itemManagerId


    // *******************TransitionFunctions***********

    // -------------------ORDERS-------------------------

    @StateTransitionFunc
    fun createNewOrder(orderCreatedEvent: OrderCreatedEvent) {
        with(orderCreatedEvent) {
            orders.put(orderId, Order(orderId, userId, createdAt, orderCart))
        }
    }

    @StateTransitionFunc
    fun setOrderDeliveryTime(event: OrderDeliveryTimeAddedEvent) {
        orders[event.orderId]?.deliveryDate = event.timeSlot
    }

//    @StateTransitionFunc
//    fun addPaymentMethod(paymentMethodAddedEvent: PaymentMethodAddedEvent) {
//        paymentMethod = paymentMethodAddedEvent.paymentMethod
//    }

    @StateTransitionFunc
    fun changeOrderState(orderStateChangedEvent: OrderStateChangedEvent) {
        orders[orderStateChangedEvent.orderId]?.state = orderStateChangedEvent.newState
    }

    @StateTransitionFunc
    fun deleteOrder(orderDeletedEvent: OrderDeletedEvent) {
        orders.remove(orderDeletedEvent.orderId)
    }

    @StateTransitionFunc
    fun notifyAbandonedCart(cartAbandonedNotifyEvent: CartAbandonedNotifyEvent) {
        // do nothing
    }

    // -------------------OrderItems-------------------------

    @StateTransitionFunc
    fun addItemToOrder(orderItemAddedEvent: OrderItemAddedEvent) {
        with(orderItemAddedEvent) {
            orders[orderId]?.orderCart?.put(itemId, 1)
        }
    }
    @StateTransitionFunc
    fun deleteItemFromOrder(orderItemDeletedEvent: OrderItemDeletedEvent) {
        with(orderItemDeletedEvent) {
            orders[orderId]?.orderCart?.remove(itemId)
        }
    }
    @StateTransitionFunc
    fun setAmountInCart(orderItemAmountChangedEvent: OrderItemAmountChangedEvent) {
        with(orderItemAmountChangedEvent) {
            orders[orderId]?.orderCart?.set(itemId, newAmount)
        }
    }

    // -------------------ITEMS-------------------------

    @StateTransitionFunc
    fun createNewItem(itemCreatedEvent: ItemCreatedEvent) {
        with(itemCreatedEvent) {
            items.put(itemId, Item(itemId, name, price, description, amountInStock))
        }
    }

    @StateTransitionFunc
    fun deleteItem(event: ItemDeletedEvent) {
        items.remove(event.itemId)
        for ((orderId, order) in orders) {
            if (order.orderCart.containsKey(event.itemId)) {
                order.orderCart.remove(event.itemId)
                // TODO: notification
            }
        }
    }

    @StateTransitionFunc
    fun setItemName(event: ItemNameUpdatedEvent) {
        items[event.itemId]?.name = event.newName
    }

    @StateTransitionFunc
    fun setItemDescription(event: ItemDescriptionUpdatedEvent) {
        items[event.itemId]?.description = event.newDescription
    }

    @StateTransitionFunc
    fun setItemPrice(event: ItemPriceUpdatedEvent) {
        items[event.itemId]?.price = event.newPrice
    }

    @StateTransitionFunc
    fun setItemStockAmount(event: ItemStockAmountUpdatedEvent) {
        items[event.itemId]?.amountInStock = event.newAmount
    }

    fun createNewOrder(userId: UUID, cart: Map<UUID, Int>, orderId: UUID = UUID.randomUUID()): OrderCreatedEvent {
        //TODO: if user exists
        if (orders.containsKey(orderId))
            throw ObjectAlreadyExistException("Order with this id already exists")

        if (cart.isEmpty())
            throw IllegalArgumentException("Can't create empty order")

        for ((itemId, itemAmount) in cart) {
            if (itemAmount < 0) throw WrongValueException("Item amount in order must not be negative!")
            if (!items.containsKey(itemId)) throw NotFoundException("No item with such id $itemId")
        }

        return OrderCreatedEvent(orderId, userId, cart.toMutableMap())
    }
    fun setOrderDeliveryTime(orderId: UUID, timeSlot: TimeSlot): OrderDeliveryTimeAddedEvent {
        checkOrderState(orderId, OrderState.BOOKED)
        return OrderDeliveryTimeAddedEvent(orderId, timeSlot)
    }

    private fun getPossibleStates(orderId: UUID) = orders[orderId]?.state.let {
        when (it) {
            null -> listOf(OrderState.COLLECTING)
            OrderState.COLLECTING -> listOf(OrderState.BOOKED, OrderState.DISCARDED)
            OrderState.BOOKED -> listOf(OrderState.COLLECTING, OrderState.PAID)
            OrderState.PAID -> listOf(OrderState.SHIPPING, OrderState.REFUND)
            OrderState.SHIPPING -> listOf(OrderState.COMPLETED, OrderState.REFUND)
            OrderState.COMPLETED -> listOf()
            OrderState.DISCARDED -> listOf()
            OrderState.REFUND -> listOf()
        }
    }

    fun changeOrderState(orderId: UUID, newState: OrderState): OrderStateChangedEvent {
        if (newState !in getPossibleStates(orderId))
            throw IllegalStateException("Can't change state from ${orders[orderId]?.state} to $newState")

        if (newState == OrderState.PAID) {
//            deliveryAddress ?: throw IllegalStateException("Can't process order without address")
            orders[orderId]?.deliveryDate ?: throw IllegalStateException("Can't process order without date")
//            orders[orderId]?.paymentMethod ?: throw IllegalStateException("Can't process order without payment method")
        }

        return OrderStateChangedEvent(orderId, newState)
    }

    fun deleteOrder(orderId: UUID): OrderDeletedEvent {
        checkOrderIdExistsThrowable(orderId)
        return OrderDeletedEvent(orderId)
    }

    private fun checkOrderState(orderId: UUID, state: OrderState) {
        if (orders[orderId]?.state != state)
            throw IllegalStateException("Wrong state of order. Order state required: ${state}, state ${orders[orderId]?.state} was found")

    }

    fun notifyAbandonedCart(orderId: UUID, time: Long): CartAbandonedNotifyEvent {
        // updateTime
        checkOrderIdExistsThrowable(orderId)
        // TODO: check enough time passed
        if (orders[orderId]?.state != OrderState.COLLECTING)
            throw IllegalStateException("Can't notify about abandoned cart, because order is in state ${orders[orderId]?.state}")

        return CartAbandonedNotifyEvent(orderId)
    }

    // -------------------OrderItems-------------------------
    fun addItemToOrder(itemId: UUID, orderId: UUID): OrderItemAddedEvent {
        checkOrderIdExistsThrowable(orderId)
        checkItemIdExistsThrowable(itemId)
        if (orders[orderId]?.orderCart?.containsKey(itemId) == true)
            setAmountInCart(itemId, orders[orderId]?.orderCart!![itemId]!! + 1, orderId)
        return OrderItemAddedEvent(itemId, orderId)
    }

    fun deleteItemFromOrder(itemId: UUID, orderId: UUID): OrderItemDeletedEvent {
        checkOrderIdExistsThrowable(orderId)
        checkItemIdExistsThrowable(itemId)
        if (orders[orderId]?.orderCart?.containsKey(itemId) != true)
            throw IllegalStateException("Can't delete item with id $itemId from order with id $orderId, because item is not in the cart")

        return OrderItemDeletedEvent(itemId, orderId)
    }

    fun setAmountInCart(itemId: UUID, newAmount: Int, orderId: UUID): OrderItemAmountChangedEvent {
        checkOrderIdExistsThrowable(orderId)
        checkItemIdExistsThrowable(itemId)
        if (orders[orderId]?.orderCart?.containsKey(itemId) != true)
            throw IllegalStateException("Can't delete item with id $itemId from order with id $orderId, because item is not in the cart")

        return OrderItemAmountChangedEvent(itemId, newAmount, orderId)
    }

    private fun checkOrderIdExistsThrowable(orderId: UUID) {
        if (!orders.containsKey(orderId)) throw NotFoundException("No order with such id $orderId ")
    }

    // -------------------ITEMS-------------------------
    fun createNewItem(
        itemId: UUID = UUID.randomUUID(),
        name: String,
        price: BigDecimal,
        description: String? = null,
        amountInStock: Int,
    ): ItemCreatedEvent {
        if (items.containsKey(itemId))
            throw ObjectAlreadyExistException("Item with this id $itemId already exists")

        if(price < BigDecimal.ZERO) throw WrongValueException("Price must not be negative!")
        if(amountInStock < 0) throw WrongValueException("Amount in stock must not be negative!")

        return ItemCreatedEvent(itemId, name, price, description, amountInStock)
    }

    fun deleteItem(itemId: UUID): ItemDeletedEvent {
        checkItemIdExistsThrowable(itemId)
        return ItemDeletedEvent(itemId)
    }

    fun setItemName(itemId: UUID, newName: String): ItemNameUpdatedEvent {
        checkItemIdExistsThrowable(itemId)
        if (newName.isBlank()) throw WrongValueException("Name of item must not be empty!")
        return ItemNameUpdatedEvent(itemId, newName)
    }

    fun setItemDescription(itemId: UUID, newDescription: String?): ItemDescriptionUpdatedEvent {
        checkItemIdExistsThrowable(itemId)
        return ItemDescriptionUpdatedEvent(itemId, newDescription)
    }

    fun setItemPrice(itemId: UUID, newPrice: BigDecimal): ItemPriceUpdatedEvent {
        checkItemIdExistsThrowable(itemId)
        if (newPrice < BigDecimal.ZERO) throw WrongValueException("Price of item must not be negative!")
        return ItemPriceUpdatedEvent(itemId, newPrice)
    }
    fun setItemStockAmount(itemId: UUID, newAmount: Int): ItemStockAmountUpdatedEvent {
        checkItemIdExistsThrowable(itemId)
        if (newAmount < 0) throw WrongValueException("Amount of item in stock must not be negative!")
        return ItemStockAmountUpdatedEvent(itemId, newAmount)
    }

    private fun checkItemIdExistsThrowable(itemId: UUID) {
        if (!items.containsKey(itemId)) throw NotFoundException("No item with such id $itemId ")
    }

}

class Order(_orderId: UUID, _userId: UUID, _creationTime: Long, _orderCart: OrderCart) {
    var orderId: UUID = _orderId
    var userId: UUID = _userId
    var creationTime: Long = _creationTime
    var state: OrderState? = null

    var deliveryDate: TimeSlot? = null
    var orderCart: OrderCart = _orderCart
//    var paymentMethod: PaymentMethod? = null

    override fun toString(): String {
        return "Order(\n" +
                " orderId=$orderId,\n" +
                " userId=$userId,\n" +
                " creationTime=$creationTime,\n" +
                " state=$state,\n" +
//               " deliveryAddress=$deliveryAddress,\n" +
//                " paymentMethod=$paymentMethod,\n" +
                " deliveryDate=$deliveryDate,\n" +
                " orderCart=[\n  ${orderCart.toString()}\n ]\n" +
                ")"
    }


}


class Item(_itemId: UUID, _name: String, _price: BigDecimal, _description: String? = null, _amountInStock: Int = 0) {
    var itemId: UUID = _itemId
    val creationTime: Long = System.currentTimeMillis()
    var name: String = _name
        set(value) {
            if (value.isNotBlank()) field = value else throw WrongValueException("Name of item must not be empty!")
        }

    var price: BigDecimal = _price
        set(value) {
            if (value > BigDecimal.ZERO) field = value else throw WrongValueException("Price must not be negative!")
        }
    var description: String? = _description
    var amountInStock: Int = _amountInStock
        set(value) {
            if (value >= 0) field = value else throw WrongValueException("Amount in stock must not be negative!")
        }

}