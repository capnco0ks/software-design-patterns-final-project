package com.eventstudio.application;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.ServicePackage;
import java.util.List;
import java.util.UUID;

/** Package subsystem. Later package configuration can extend this catalog. */
public final class PackageCatalog {
    private final ServicePackageRepository repository;

    public PackageCatalog(ServicePackageRepository repository) {
        this.repository = repository;
    }

    public ServicePackage createStarter(Event event) {
        List<ServicePackage> existing = repository.findByEventId(event.id());
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("У мероприятия уже есть пакет услуг");
        }
        ServicePackage starter = new ServicePackage(UUID.randomUUID(), event.id(), "Стартовый пакет", 0);
        repository.save(starter);
        return starter;
    }

    public List<ServicePackage> listForEvent(UUID eventId) {
        return repository.findByEventId(eventId);
    }
}
