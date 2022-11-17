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
    fun deleteNewItem(itemDeletedEvent: ItemDeletedEvent) {
        items.remove(itemDeletedEvent.itemId)
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



