package shopDemo.orders.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.quipy.core.EventSourcingService
import ru.quipy.core.EventSourcingServiceFactory
import shopDemo.orders.impl.entity.ItemManagerAggregate
import shopDemo.orders.impl.entity.Order
import java.util.*


@Configuration
class ShopDemoConfig {
    @Autowired
    private lateinit var eventSourcingServiceFactory: EventSourcingServiceFactory

    @Bean
    fun orderESService(): EventSourcingService<UUID, ItemManagerAggregate, Order> =
        eventSourcingServiceFactory.create()
}