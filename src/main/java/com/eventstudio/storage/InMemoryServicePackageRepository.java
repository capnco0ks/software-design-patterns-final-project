package com.eventstudio.storage;

import com.eventstudio.application.ServicePackageRepository;
import com.eventstudio.domain.ServicePackage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InMemoryServicePackageRepository implements ServicePackageRepository {
    private final Map<UUID, ServicePackage> packages = new LinkedHashMap<>();

    @Override
    public synchronized List<ServicePackage> findByEventId(UUID eventId) {
        List<ServicePackage> result = new ArrayList<>();
        for (ServicePackage item : packages.values()) {
            if (item.eventId().equals(eventId)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public synchronized void save(ServicePackage servicePackage) {
        packages.put(servicePackage.id(), servicePackage);
    }
}
