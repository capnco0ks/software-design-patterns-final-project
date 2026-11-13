package com.eventstudio.web;

import com.eventstudio.application.BookingOverview;
import com.eventstudio.application.EventOverview;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.PricingMode;
import java.util.List;

final class BookingPage {
    private BookingPage() {
    }

    static String form(EventOverview overview, String error) {
        StringBuilder body = new StringBuilder();
        body.append("<a class='back' href='/'>← К мероприятиям</a><section class='panel narrow'>")
            .append("<h1>Новое бронирование</h1><p>")
            .append(EventPage.escape(overview.event().title())).append(" · ")
            .append(EventPage.escape(overview.starterPackage().name())).append("</p>");
        if (error != null) {
            body.append("<p class='message error' role='alert'>").append(EventPage.escape(error)).append("</p>");
        }
        body.append("<form method='post' action='/bookings'><input type='hidden' name='eventId' value='")
            .append(overview.event().id()).append("'><input type='hidden' name='packageId' value='")
            .append(overview.starterPackage().id()).append("'>")
            .append("<label>Имя участника<input name='name' maxlength='120' required></label>")
            .append("<label>Email<input type='email' name='email' maxlength='200' required></label>")
            .append("<label>Способ расчёта<select name='pricingMode'>");
        for (PricingMode mode : PricingMode.values()) {
            body.append("<option value='").append(mode.name()).append("'>")
                .append(EventPage.escape(mode.label())).append("</option>");
        }
        body.append("</select></label><p class='hint'>Пакет: ")
            .append(overview.starterPackage().basePriceCents() / 100)
            .append(" ₸. Сервисный сбор добавляет 10%.</p>")
            .append("<button type='submit'>Создать бронь</button></form></section>");
        return EventPage.layout("Бронирование", body.toString());
    }

    static String section(List<BookingOverview> bookings, List<String> events) {
        StringBuilder body = new StringBuilder("<div class='stack'><section class='panel'><h2>Бронирования</h2>");
        if (bookings.isEmpty()) {
            body.append("<p class='empty'>Пока нет бронирований.</p>");
        }
        for (BookingOverview overview : bookings) {
            var booking = overview.booking();
            body.append("<article class='booking'><strong>")
                .append(EventPage.escape(overview.participant().name()))
                .append("</strong><p>").append(EventPage.escape(overview.event().title()))
                .append(" · ").append(EventPage.escape(overview.servicePackage().name()))
                .append("</p><p>Расчёт: ").append(EventPage.escape(booking.pricingMode().label()))
                .append(" · Итого: ").append(booking.totalPriceCents() / 100)
                .append(" ₸ · Статус: ").append(booking.status()).append("</p>");
            if (booking.status() != BookingStatus.CANCELLED) {
                if (booking.status() == BookingStatus.NEW) {
                    statusButton(body, booking.id().toString(), BookingStatus.CONFIRMED, "Подтвердить");
                }
                statusButton(body, booking.id().toString(), BookingStatus.CANCELLED, "Отменить");
            }
            body.append("</article>");
        }
        body.append("</section><section class='panel'><h2>Журнал событий</h2>");
        if (events.isEmpty()) {
            body.append("<p class='empty'>Смена статуса появится здесь.</p>");
        } else {
            body.append("<ol class='log'>");
            for (String event : events) {
                body.append("<li>").append(EventPage.escape(event)).append("</li>");
            }
            body.append("</ol>");
        }
        return body.append("</section></div>").toString();
    }

    private static void statusButton(StringBuilder body, String bookingId,
                                     BookingStatus status, String label) {
        body.append("<form class='inline-form' method='post' action='/bookings/")
            .append(bookingId).append("/status'><input type='hidden' name='status' value='")
            .append(status.name()).append("'><button type='submit'>")
            .append(label).append("</button></form> ");
    }
}
