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
    fun createNewOrder(
        orderId: UUID,
        userId: UUID,
        orderCart: OrderCart
    ): OrderCreatedEvent {
        val orderCreatedEvent = OrderCreatedEvent(orderId, userId, orderCart)
        with(orderCreatedEvent) {
            orders.put(orderId, Order(orderId, userId, createdAt, orderCart))
        }
        return orderCreatedEvent
    }

    @StateTransitionFunc
    fun setOrderDeliveryTime(
        orderId: UUID,
        timeSlot: TimeSlot,
    ): OrderDeliveryTimeAddedEvent {
        orders[orderId]?.deliveryDate = timeSlot
        return OrderDeliveryTimeAddedEvent(orderId, timeSlot)
    }

//    @StateTransitionFunc
//    fun addPaymentMethod(paymentMethodAddedEvent: PaymentMethodAddedEvent) {
//        paymentMethod = paymentMethodAddedEvent.paymentMethod
//    }

    @StateTransitionFunc
    fun changeOrderState(
        orderId: UUID,
        newState: OrderState
    ): OrderStateChangedEvent {
        orders[orderId]?.state = newState
        return OrderStateChangedEvent(orderId, newState)
    }

    @StateTransitionFunc
    fun deleteOrder(
        orderId: UUID
    ): OrderDeletedEvent {
        orders.remove(orderId)
        return OrderDeletedEvent(orderId)
    }

    @StateTransitionFunc
    fun notifyAbandonedCart(
        orderId: UUID
    ): CartAbandonedNotifyEvent {
        return CartAbandonedNotifyEvent(orderId)
    }

    // -------------------OrderItems-------------------------

    @StateTransitionFunc
    fun addItemToOrder(
        itemId: UUID,
        orderId: UUID
    ): OrderItemAddedEvent {
        orders[orderId]?.orderCart?.put(itemId, 1)
        return OrderItemAddedEvent(itemId, orderId)
    }
    @StateTransitionFunc
    fun deleteItemFromOrder(
        itemId: UUID,
        orderId: UUID
    ): OrderItemDeletedEvent {
        orders[orderId]?.orderCart?.remove(itemId)
        return OrderItemDeletedEvent(itemId, orderId)
    }
    @StateTransitionFunc
    fun setAmountInCart(
        itemId: UUID,
        newAmount: Int,
        orderId: UUID
    ): OrderItemAmountChangedEvent {
        orders[orderId]?.orderCart?.set(itemId, newAmount)
        return OrderItemAmountChangedEvent(itemId, newAmount, orderId)
    }

    // -------------------ITEMS-------------------------

    @StateTransitionFunc
    fun createNewItem(
        itemId: UUID,
        itemName: String,
        price: BigDecimal,
        description: String?,
        amountInStock: Int
    ): ItemCreatedEvent {
        val itemCreatedEvent = ItemCreatedEvent(itemId, itemName, price, description, amountInStock)
        with(itemCreatedEvent) {
            items.put(itemId, Item(itemId, name, price, description, amountInStock))
        }
        return itemCreatedEvent
    }

    @StateTransitionFunc
    fun deleteItem(
        itemId: UUID
    ): ItemDeletedEvent {
        items.remove(itemId)
        for ((orderId, order) in orders) {
            if (order.orderCart.containsKey(itemId)) {
                order.orderCart.remove(itemId)
                // TODO: notification
            }
        }
        return ItemDeletedEvent(itemId)
    }

    @StateTransitionFunc
    fun setItemName(
        itemId: UUID,
        newName: String,
    ): ItemNameUpdatedEvent {
        items[itemId]?.name = newName
        return ItemNameUpdatedEvent(itemId, newName)
    }

    @StateTransitionFunc
    fun setItemDescription(
        itemId: UUID,
        newDescription: String?
    ): ItemDescriptionUpdatedEvent {
        items[itemId]?.description = newDescription
        return ItemDescriptionUpdatedEvent(itemId, newDescription)
    }

    @StateTransitionFunc
    fun setItemPrice(
        itemId: UUID,
        newPrice: BigDecimal
    ): ItemPriceUpdatedEvent {
        items[itemId]?.price = newPrice
        return ItemPriceUpdatedEvent(itemId, newPrice)
    }

    @StateTransitionFunc
    fun setItemStockAmount(
        itemId: UUID,
        newAmount: Int
    ): ItemStockAmountUpdatedEvent {
        items[itemId]?.amountInStock = newAmount
        return ItemStockAmountUpdatedEvent(itemId, newAmount)
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

//enum class PaymentMethod {
//    Cash,
//    Card
//}



