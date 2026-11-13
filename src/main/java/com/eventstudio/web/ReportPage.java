package com.eventstudio.web;

import com.eventstudio.application.report.ReportSnapshot;

final class ReportPage {
    private ReportPage() {
    }

    static String show(ReportSnapshot report) {
        String body = "<a class='back' href='/'>← К мероприятиям</a>"
            + "<section class='panel narrow'><h1>Итоговая сводка</h1>"
            + "<p>Мероприятий: " + report.events() + "</p>"
            + "<p>Пакетов: " + report.packages() + "</p>"
            + "<p>Бронирований: " + report.bookings() + "</p>"
            + "<p>Подтверждено: " + report.confirmed() + "</p>"
            + "<p>Отменено: " + report.cancelled() + "</p>"
            + "<p><strong>Выручка подтверждённых броней: "
            + report.confirmedRevenueCents() / 100 + " ₸</strong></p>"
            + "<a href='/report.csv'>Скачать CSV</a></section>";
        return EventPage.layout("Итоговая сводка", body);
    }
}
