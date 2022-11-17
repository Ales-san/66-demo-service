package shopDemo.orders.impl.entity

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "itemManagement")
class ItemManagerAggregate : Aggregate