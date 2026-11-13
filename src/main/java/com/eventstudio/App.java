package com.eventstudio;

import com.eventstudio.application.EventCatalog;
import com.eventstudio.application.EventPlanningFacade;
import com.eventstudio.application.PackageCatalog;
import com.eventstudio.application.BookingEventLog;
import com.eventstudio.application.BookingService;
import com.eventstudio.application.importing.BookingImportService;
import com.eventstudio.application.importing.CsvBookingAdapter;
import com.eventstudio.application.report.ReportService;
import com.eventstudio.domain.EventType;
import com.eventstudio.storage.FileDataStore;
import com.eventstudio.web.EventController;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.nio.file.Path;
import java.util.List;

public final class App {
    private App() {
    }

    public static void main(String[] args) throws Exception {
        FileDataStore store = new FileDataStore(Path.of(
            System.getProperty("eventstudio.data", "data/local/event-studio.bin")));
        BookingEventLog bookingLog = new BookingEventLog(store.bookingEvents(), store::appendBookingEvent);
        BookingService bookingService = new BookingService(store, store, store, store, List.of(bookingLog));
        EventPlanningFacade planning = new EventPlanningFacade(
            new EventCatalog(store), new PackageCatalog(store),
            bookingService, bookingLog,
            new BookingImportService(new CsvBookingAdapter(), bookingService),
            new ReportService(store, store, store));
        if (store.findAll().isEmpty()) {
            planning.createEvent("Открытый мастер-класс", LocalDate.now().plusDays(7), 30, EventType.WORKSHOP);
            planning.createEvent("Студенческий концерт", LocalDate.now().plusDays(14), 100, EventType.CONCERT);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new EventController(planning));
        server.start();
        System.out.println("Event Studio: http://127.0.0.1:8080");
        System.out.println("Остановить сервер: Ctrl+C");
    }
}
