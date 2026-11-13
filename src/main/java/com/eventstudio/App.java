package com.eventstudio;

import com.eventstudio.application.EventCatalog;
import com.eventstudio.domain.EventType;
import com.eventstudio.storage.InMemoryEventRepository;
import com.eventstudio.web.EventController;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.LocalDate;

public final class App {
    private App() {
    }

    public static void main(String[] args) throws Exception {
        EventCatalog catalog = new EventCatalog(new InMemoryEventRepository());
        catalog.create("Открытый мастер-класс", LocalDate.now().plusDays(7), 30, EventType.WORKSHOP);
        catalog.create("Студенческий концерт", LocalDate.now().plusDays(14), 100, EventType.CONCERT);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new EventController(catalog));
        server.start();
        System.out.println("Event Studio: http://127.0.0.1:8080");
        System.out.println("Остановить сервер: Ctrl+C");
    }
}
