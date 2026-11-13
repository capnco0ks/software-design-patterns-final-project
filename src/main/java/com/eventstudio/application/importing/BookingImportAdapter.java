package com.eventstudio.application.importing;

import java.util.List;

public interface BookingImportAdapter {
    List<BookingImportRow> parse(String externalText);
}
