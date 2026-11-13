package com.eventstudio.web;

import com.eventstudio.application.EventPlanningFacade;
import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.PricingMode;
import com.eventstudio.application.EventOverview;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class EventController implements HttpHandler {
    private final EventPlanningFacade planning;

    public EventController(EventPlanningFacade planning) {
        this.planning = planning;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (method.equals("GET") && (path.equals("/") || path.equals("/events"))) {
            String notice = switch (String.valueOf(exchange.getRequestURI().getQuery())) {
                case "ok=created" -> "Мероприятие создано";
                case "ok=updated" -> "Изменения сохранены";
                case "ok=booked" -> "Бронирование создано";
                case "ok=status" -> "Статус бронирования изменён";
                case "ok=imported" -> "Бронирования импортированы";
                default -> null;
            };
            send(exchange, 200, home(null, notice));
            return;
        }
        if (method.equals("POST") && path.equals("/events")) {
            try {
                Map<String, String> form = readForm(exchange);
                planning.createEvent(form.get("title"), parseDate(form.get("date")), parseCapacity(form.get("capacity")), parseType(form.get("type")));
                redirect(exchange, "/?ok=created");
            } catch (IllegalArgumentException exception) {
                send(exchange, 400, home(exception.getMessage(), null));
            }
            return;
        }
        if (method.equals("GET") && path.equals("/bookings/new")) {
            try {
                UUID eventId = UUID.fromString(queryValue(exchange.getRequestURI().getRawQuery(), "event"));
                EventOverview overview = planning.listEvents().stream()
                    .filter(item -> item.event().id().equals(eventId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));
                send(exchange, 200, BookingPage.form(overview, null));
            } catch (IllegalArgumentException exception) {
                send(exchange, 404, EventPage.notFound());
            }
            return;
        }
        if (method.equals("POST") && path.equals("/bookings")) {
            try {
                Map<String, String> form = readForm(exchange);
                planning.createBooking(UUID.fromString(form.get("eventId")), UUID.fromString(form.get("packageId")),
                    form.get("name"), form.get("email"), PricingMode.valueOf(form.get("pricingMode")));
                redirect(exchange, "/?ok=booked");
            } catch (IllegalArgumentException | NullPointerException exception) {
                send(exchange, 400, home("Проверьте данные бронирования: " + exception.getMessage(), null));
            }
            return;
        }
        if (method.equals("POST") && path.equals("/bookings/import")) {
            try {
                planning.importBookings(readForm(exchange).get("csv"));
                redirect(exchange, "/?ok=imported");
            } catch (IllegalArgumentException exception) {
                send(exchange, 400, home(exception.getMessage(), null));
            }
            return;
        }
        if (method.equals("GET") && path.equals("/report")) {
            send(exchange, 200, ReportPage.show(planning.report()));
            return;
        }
        if (method.equals("GET") && path.equals("/report.csv")) {
            sendCsv(exchange, planning.exportReportCsv());
            return;
        }
        String[] parts = path.split("/");
        if (parts.length == 4 && parts[1].equals("bookings") && parts[3].equals("status")
            && method.equals("POST")) {
            try {
                Map<String, String> form = readForm(exchange);
                planning.changeBookingStatus(UUID.fromString(parts[2]), BookingStatus.valueOf(form.get("status")));
                redirect(exchange, "/?ok=status");
            } catch (IllegalArgumentException | NullPointerException exception) {
                send(exchange, 400, home("Проверьте статус бронирования: " + exception.getMessage(), null));
            }
            return;
        }
        if (parts.length == 4 && parts[1].equals("events") && parts[3].equals("edit")) {
            try {
                UUID id = UUID.fromString(parts[2]);
                Optional<Event> found = planning.findEvent(id);
                if (found.isEmpty()) {
                    send(exchange, 404, EventPage.notFound());
                    return;
                }
                if (method.equals("GET")) {
                    send(exchange, 200, EventPage.edit(found.get(), null));
                    return;
                }
                if (method.equals("POST")) {
                    try {
                        Map<String, String> form = readForm(exchange);
                        planning.updateEvent(id, form.get("title"), parseDate(form.get("date")), parseCapacity(form.get("capacity")), parseType(form.get("type")));
                        redirect(exchange, "/?ok=updated");
                    } catch (IllegalArgumentException exception) {
                        send(exchange, 400, EventPage.edit(found.get(), exception.getMessage()));
                    }
                    return;
                }
            } catch (IllegalArgumentException ignored) {
                // An invalid event ID is a missing page, not a server error.
            }
        }
        send(exchange, 404, EventPage.notFound());
    }

    private String home(String error, String notice) {
        return EventPage.home(planning.listEvents(), planning.listBookings(), planning.bookingEvents(), error, notice);
    }

    private static String queryValue(String query, String key) {
        if (query == null) {
            throw new IllegalArgumentException("Параметр не найден");
        }
        for (String entry : query.split("&")) {
            String[] pair = entry.split("=", 2);
            if (pair[0].equals(key) && pair.length == 2) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        throw new IllegalArgumentException("Параметр не найден");
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readNBytes(8193);
        if (bytes.length > 8192) {
            throw new IllegalArgumentException("Форма слишком большая");
        }
        String body = new String(bytes, StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();
        for (String entry : body.split("&")) {
            if (!entry.isEmpty()) {
                String[] pair = entry.split("=", 2);
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                form.put(key, value);
            }
        }
        return form;
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException("Выберите корректную дату мероприятия");
        }
    }

    private static int parseCapacity(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Введите количество мест числом");
        }
    }

    private static EventType parseType(String value) {
        try {
            return EventType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Выберите корректный тип мероприятия");
        }
    }

    private static void send(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void sendCsv(HttpExchange exchange, String csv) throws IOException {
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=event-studio-report.csv");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void redirect(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Location", path);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }
}
