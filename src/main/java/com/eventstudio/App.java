package com.eventstudio;

import com.eventstudio.application.EventCatalog;
import com.eventstudio.application.EventPlanningFacade;
import com.eventstudio.application.PackageCatalog;
import com.eventstudio.domain.EventType;
import com.eventstudio.storage.InMemoryEventRepository;
import com.eventstudio.storage.InMemoryServicePackageRepository;
import com.eventstudio.web.EventController;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.LocalDate;

public final class App {
    private App() {
    }

    public static void main(String[] args) throws Exception {
        EventPlanningFacade planning = new EventPlanningFacade(
            new EventCatalog(new InMemoryEventRepository()),
            new PackageCatalog(new InMemoryServicePackageRepository())
        );
        planning.createEvent("Открытый мастер-класс", LocalDate.now().plusDays(7), 30, EventType.WORKSHOP);
        planning.createEvent("Студенческий концерт", LocalDate.now().plusDays(14), 100, EventType.CONCERT);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new EventController(planning));
        server.start();
        System.out.println("Event Studio: http://127.0.0.1:8080");
        System.out.println("Остановить сервер: Ctrl+C");
    }
}
