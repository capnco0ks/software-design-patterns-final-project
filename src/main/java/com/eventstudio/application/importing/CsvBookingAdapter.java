package com.eventstudio.application.importing;

import com.eventstudio.domain.PricingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Adapter from an external five-column CSV into the application's booking request model. */
public final class CsvBookingAdapter implements BookingImportAdapter {
    private static final List<String> HEADER =
        List.of("event_id", "package_id", "name", "email", "pricing_mode");

    @Override
    public List<BookingImportRow> parse(String externalText) {
        if (externalText == null || externalText.isBlank()) {
            throw new IllegalArgumentException("Вставьте CSV с заголовком и хотя бы одной строкой");
        }
        String[] lines = externalText.strip().replace("\r\n", "\n").split("\n", -1);
        if (!columns(lines[0].replaceFirst("^\uFEFF", "")).equals(HEADER)) {
            throw new IllegalArgumentException("Заголовок CSV: event_id,package_id,name,email,pricing_mode");
        }
        List<BookingImportRow> result = new ArrayList<>();
        for (int line = 1; line < lines.length; line++) {
            if (lines[line].isBlank()) {
                continue;
            }
            List<String> values = columns(lines[line]);
            if (values.size() != 5) {
                throw new IllegalArgumentException("Строка " + (line + 1) + ": нужно пять колонок");
            }
            try {
                result.add(new BookingImportRow(UUID.fromString(values.get(0).strip()),
                    UUID.fromString(values.get(1).strip()), values.get(2).strip(),
                    values.get(3).strip(), PricingMode.valueOf(values.get(4).strip())));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Строка " + (line + 1) + ": неверный ID или режим расчёта");
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("В CSV нет строк бронирования");
        }
        return List.copyOf(result);
    }

    private static List<String> columns(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    cell.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                result.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Незакрытая кавычка в CSV");
        }
        result.add(cell.toString());
        return result;
    }
}
