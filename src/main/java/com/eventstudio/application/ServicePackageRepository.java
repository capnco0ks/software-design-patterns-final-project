package com.eventstudio.application;

import com.eventstudio.domain.ServicePackage;
import java.util.List;
import java.util.UUID;

public interface ServicePackageRepository {
    List<ServicePackage> findByEventId(UUID eventId);

    void save(ServicePackage servicePackage);
}
