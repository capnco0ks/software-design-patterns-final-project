# UML и поток вызовов

Диаграммы отражают текущий код. Пакет пока стартовый; Builder и Decorator Бакытжана ещё не добавлены.

## Диаграмма классов

```mermaid
classDiagram
  class Event
  class WorkshopEvent
  class ConcertEvent
  class EventCreator
  class WorkshopEventCreator
  class ConcertEventCreator
  class EventPlanningFacade
  class EventCatalog
  class PackageCatalog
  class BookingService
  class PricingStrategy
  class StandardPricing
  class ServiceFeePricing
  class BookingObserver
  class BookingEventLog
  class BookingImportAdapter
  class CsvBookingAdapter
  class ReportVisitor
  class SummaryReportVisitor
  class FileDataStore
  Event <|.. WorkshopEvent
  Event <|.. ConcertEvent
  EventCreator <|-- WorkshopEventCreator
  EventCreator <|-- ConcertEventCreator
  EventCatalog --> EventCreator
  EventPlanningFacade --> EventCatalog
  EventPlanningFacade --> PackageCatalog
  EventPlanningFacade --> BookingService
  BookingService --> PricingStrategy
  PricingStrategy <|.. StandardPricing
  PricingStrategy <|.. ServiceFeePricing
  BookingService --> BookingObserver
  BookingObserver <|.. BookingEventLog
  BookingImportAdapter <|.. CsvBookingAdapter
  ReportVisitor <|.. SummaryReportVisitor
  EventCatalog --> FileDataStore
  PackageCatalog --> FileDataStore
  BookingService --> FileDataStore
```

## Диаграмма последовательности: бронирование и итог

```mermaid
sequenceDiagram
  actor User as Организатор
  participant UI as EventController
  participant Facade as EventPlanningFacade
  participant Booking as BookingService
  participant Strategy as PricingStrategy
  participant Store as FileDataStore
  participant Observer as BookingEventLog
  participant Report as ReportService
  participant Visitor as SummaryReportVisitor
  User->>UI: Выбрать пакет и способ расчёта
  UI->>Facade: createBooking(...)
  Facade->>Booking: create(...)
  Booking->>Strategy: calculate(basePriceCents)
  Strategy-->>Booking: totalPriceCents
  Booking->>Store: save(participant), save(booking)
  Booking-->>UI: Booking
  User->>UI: Подтвердить бронь
  UI->>Facade: changeBookingStatus(id, CONFIRMED)
  Facade->>Booking: changeStatus(...)
  Booking->>Store: save(booking)
  Booking->>Observer: statusChanged(...)
  Observer->>Store: appendBookingEvent(...)
  User->>UI: Открыть итоговую сводку
  UI->>Report: snapshot()
  Report->>Store: findAll(), findByEventId(), findAllBookings()
  Report->>Visitor: accept() для Event, ServicePackage, Booking
  Visitor-->>UI: числа и выручка
```
