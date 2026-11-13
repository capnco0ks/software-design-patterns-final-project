package com.eventstudio.application;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.ServicePackage;

public record EventOverview(Event event, ServicePackage starterPackage) {
}
