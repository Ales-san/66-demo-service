package shopDemo.orders.impl.entity

import java.time.LocalDate

data class TimeSlot (
    var startDate: LocalDate,
    var endDate: LocalDate
    ) {

//    constructor(startDate: LocalDate, endDate: LocalDate) {
//        this.startDate = startDate;
//        this.endDate = endDate;
//    }

    fun updateStartDate (date: LocalDate) {
        startDate = date;
    }

    fun updateEndDate (date: LocalDate) {
        endDate = date;
    }
}