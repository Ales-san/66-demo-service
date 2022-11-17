package shopDemo.orders.impl.entity

import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.common.exception.ObjectAlreadyExistException
import com.itmo.microservices.demo.common.exception.WrongValueException
import shopDemo.orders.api.*
import shopDemo.orders.api.model.OrderState
import java.math.BigDecimal
import java.util.*

// -------------------ORDERS-------------------------
fun ItemManager.createNewOrder(userId: UUID, cart: Map<UUID, Int>, orderId: UUID = UUID.randomUUID()): OrderCreatedEvent {
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
fun ItemManager.setOrderDeliveryTime(timeSlot: TimeSlot, orderId: UUID): OrderDeliveryTimeAddedEvent {
    checkOrderState(orderId, OrderState.BOOKED)
    return OrderDeliveryTimeAddedEvent(orderId, timeSlot)
}

private fun ItemManager.getPossibleStates(orderId: UUID) = orders[orderId]?.state.let {
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

fun ItemManager.changeOrderState(newState: OrderState, orderId: UUID): OrderStateChangedEvent {
    if (newState !in getPossibleStates(orderId))
        throw IllegalStateException("Can't change state from ${orders[orderId]?.state} to $newState")

    if (newState == OrderState.PAID) {
//            deliveryAddress ?: throw IllegalStateException("Can't process order without address")
        orders[orderId]?.deliveryDate ?: throw IllegalStateException("Can't process order without date")
//            orders[orderId]?.paymentMethod ?: throw IllegalStateException("Can't process order without payment method")
    }

    return OrderStateChangedEvent(orderId, newState)
}

fun ItemManager.deleteOrder(orderId: UUID): OrderDeletedEvent {
    checkOrderIdExistsThrowable(orderId)
    return OrderDeletedEvent(orderId)
}

private fun ItemManager.checkOrderState(orderId: UUID, state: OrderState) {
    if (orders[orderId]?.state != state)
        throw IllegalStateException("Wrong state of order. Order state required: ${state}, state ${orders[orderId]?.state} was found")

}

fun ItemManager.notifyAbandonedCart(time: Int, orderId: UUID): CartAbandonedNotifyEvent {
    // updateTime
    checkOrderIdExistsThrowable(orderId)
    // TODO: check enough time passed
    if (orders[orderId]?.state != OrderState.COLLECTING)
        throw IllegalStateException("Can't notify about abandoned cart, because order is in state ${orders[orderId]?.state}")

    return CartAbandonedNotifyEvent(orderId)
}

fun ItemManager.addItemToOrder(itemId: UUID, orderId: UUID): OrderItemAddedEvent {
    checkOrderIdExistsThrowable(orderId)
    checkItemIdExistsThrowable(itemId)
    return OrderItemAddedEvent(itemId, orderId)
}

fun ItemManager.deleteItemFromOrder(itemId: UUID, orderId: UUID): OrderItemDeletedEvent {
    checkOrderIdExistsThrowable(orderId)
    checkItemIdExistsThrowable(itemId)
    if (orders[orderId]?.orderCart?.containsKey(itemId) != true)
        throw IllegalStateException("Can't delete item with id $itemId from order with id $orderId, because item is not in the cart")

    return OrderItemDeletedEvent(itemId, orderId)
}

fun ItemManager.setAmountInCart(itemId: UUID, newAmount: Int, orderId: UUID): OrderItemAmountChangedEvent {
    checkOrderIdExistsThrowable(orderId)
    checkItemIdExistsThrowable(itemId)

    return OrderItemAmountChangedEvent(itemId, newAmount, orderId)
}

private fun ItemManager.checkOrderIdExistsThrowable(orderId: UUID) {
    if (!orders.containsKey(orderId)) throw NotFoundException("No order with such id $orderId ")
}

// -------------------ITEMS-------------------------
fun ItemManager.createNewItem(
    name: String,
    price: BigDecimal,
    amountInStock: Int,
    description: String? = null,
    itemId: UUID = UUID.randomUUID(),
): ItemCreatedEvent {
    if (items.containsKey(itemId))
        throw ObjectAlreadyExistException("Item with this id $itemId already exists")

    if(price < BigDecimal.ZERO) throw WrongValueException("Price must not be negative!")
    if(amountInStock < 0) throw WrongValueException("Amount in stock must not be negative!")

    return ItemCreatedEvent(itemId, name, price, description, amountInStock)
}

fun ItemManager.deleteItem(itemId: UUID): ItemDeletedEvent {
    checkItemIdExistsThrowable(itemId)
    return ItemDeletedEvent(itemId)
}

fun ItemManager.setItemName(itemId: UUID, newName: String): ItemNameUpdatedEvent {
    checkItemIdExistsThrowable(itemId)
    if (newName.isBlank()) throw WrongValueException("Name of item must not be empty!")
    return ItemNameUpdatedEvent(itemId, newName)
}

fun ItemManager.setItemDescription(itemId: UUID, newDescription: String?): ItemDescriptionUpdatedEvent {
    checkItemIdExistsThrowable(itemId)
    return ItemDescriptionUpdatedEvent(itemId, newDescription)
}

fun ItemManager.setItemPrice(itemId: UUID, newPrice: BigDecimal): ItemPriceUpdatedEvent {
    checkItemIdExistsThrowable(itemId)
    if (newPrice < BigDecimal.ZERO) throw WrongValueException("Price of item must not be negative!")
    return ItemPriceUpdatedEvent(itemId, newPrice)
}
fun ItemManager.setItemStockAmount(itemId: UUID, newAmount: Int): ItemStockAmountUpdatedEvent {
    checkItemIdExistsThrowable(itemId)
    if (newAmount < 0) throw WrongValueException("Amount of item in stock must not be negative!")
    return ItemStockAmountUpdatedEvent(itemId, newAmount)
}

private fun ItemManager.checkItemIdExistsThrowable(itemId: UUID) {
    if (!items.containsKey(itemId)) throw NotFoundException("No item with such id $itemId ")
}
