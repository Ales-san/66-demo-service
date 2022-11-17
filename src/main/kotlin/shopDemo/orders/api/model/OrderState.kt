package shopDemo.orders.api.model

//enum class OrderState {
//    Collected,
//    InProcess,
//    Processed,
//    Paid,
//    Delivered,
//    Discarded
//}

enum class OrderState {
    COLLECTING,
    BOOKED,
    PAID,
    DISCARDED,
    SHIPPING,
    COMPLETED,
    REFUND,
}