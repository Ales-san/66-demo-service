package shopDemo.orders.impl.subcribers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.quipy.core.EventSourcingService
import ru.quipy.streams.AggregateSubscriptionsManager
import shopDemo.orders.api.*
import shopDemo.orders.impl.entity.ItemManager
import shopDemo.orders.impl.entity.ItemManagerAggregate
import java.util.*
import javax.annotation.PostConstruct

class ItemManagerSubscriber(
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val itemManagersService: EventSourcingService<UUID, ItemManagerAggregate, ItemManager>
) {
    private val logger: Logger = LoggerFactory.getLogger(ItemManagerSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ItemManagerAggregate::class, "transactions::bank-accounts-subscriber") {
            `when`(OrderCreatedEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.createNewOrder(
                            event.orderId,
                            event.orderCart,
                            event.userId
                        )
                }
            }
            `when`(OrderStateChangedEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.changeOrderState(event.orderId, event.newState)
                }
            }
            `when`(OrderDeliveryTimeAddedEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.setOrderDeliveryTime(event.orderId, event.timeSlot)
                }
            }
            `when`(OrderDeletedEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.deleteOrder(event.orderId)
                }
            }
            `when`(CartAbandonedNotifyEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.notifyAbandonedCart(event.orderId, event.createdAt)
                }
            }
            `when`(OrderItemAddedEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.addItemToOrder(event.orderId, event.itemId)
                }
            }
            `when`(OrderItemDeletedEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.deleteItemFromOrder(event.orderId, event.itemId)
                }
            }
            `when`(OrderItemAmountChangedEvent::class) { event ->
                itemManagersService.update(event.orderId) {
                    it.setAmountInCart(event.orderId, event.newAmount, event.itemId)
                }
            }
            `when`(ItemCreatedEvent::class) { event ->
                itemManagersService.update(event.itemId) {
                    it.createNewItem(event.itemId, event.itemName, event.price, event.description, event.amountInStock)
                }
            }
            `when`(ItemDeletedEvent::class) { event ->
                itemManagersService.update(event.itemId) {
                    it.deleteItem(event.itemId)
                }
            }
            `when`(ItemNameUpdatedEvent::class) { event ->
                itemManagersService.update(event.itemId) {
                    it.setItemName(event.itemId, event.newName)
                }
            }
            `when`(ItemDescriptionUpdatedEvent::class) { event ->
                itemManagersService.update(event.itemId) {
                    it.setItemDescription(event.itemId, event.newDescription)
                }
            }
            `when`(ItemPriceUpdatedEvent::class) { event ->
                itemManagersService.update(event.itemId) {
                    it.setItemPrice(event.itemId, event.newPrice)
                }
            }
            `when`(ItemStockAmountUpdatedEvent::class) { event ->
                itemManagersService.update(event.itemId) {
                    it.setItemStockAmount(event.itemId, event.newAmount)
                }
            }
        }
    }
}