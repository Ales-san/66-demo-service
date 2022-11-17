package shopDemo.orders.api.model

import java.util.*

//@JvmInline
//value class OrderCart(val value: MutableMap<UUID, Int>) : MutableMap {
//    override fun toString(): String {
//        return super.toString()
//    }
//}

typealias OrderCart = MutableMap<UUID, Int>