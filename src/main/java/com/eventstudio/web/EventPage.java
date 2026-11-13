package com.eventstudio.web;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import com.eventstudio.application.EventOverview;
import java.time.LocalDate;
import java.util.List;

final class EventPage {
    private EventPage() {
    }

    static String home(List<EventOverview> events, String error, String notice) {
        StringBuilder body = new StringBuilder();
        body.append("<section class='intro'><p class='eyebrow'>Software Design Patterns</p>")
            .append("<h1>Event Studio</h1><p>Создавайте мероприятия и управляйте их данными. ")
            .append("При создании сразу появляется стартовый пакет услуг.</p></section>");
        body.append(message(error, notice));
        body.append("<div class='grid'><section class='panel'><h2>Новое мероприятие</h2>")
            .append("<form method='post' action='/events'>")
            .append("<label>Название<input name='title' maxlength='120' required placeholder='Например, воркшоп по дизайну'></label>")
            .append("<label>Дата<input name='date' type='date' required value='")
            .append(LocalDate.now().plusDays(7)).append("'></label>")
            .append("<label>Количество мест<input name='capacity' type='number' min='1' max='5000' required value='30'></label>")
            .append("<label>Тип<select name='type'>").append(typeOptions(null)).append("</select></label>")
            .append("<p class='hint'>Мастер-класс: до 60 мест. Концерт: до 5000 мест.</p>")
            .append("<button type='submit'>Создать мероприятие</button></form></section>")
            .append("<section class='panel'><div class='section-heading'><h2>Мероприятия</h2><span>")
            .append(events.size()).append("</span></div>");
        if (events.isEmpty()) {
            body.append("<p class='empty'>Мероприятий пока нет.</p>");
        } else {
            body.append("<div class='event-list'>");
            for (EventOverview overview : events) {
                Event event = overview.event();
                body.append("<article class='event'><div><span class='pill'>")
                    .append(escape(event.type().label())).append("</span><h3>")
                    .append(escape(event.title())).append("</h3><p>")
                    .append(event.date()).append(" · ").append(event.capacity())
                    .append(" мест</p><p class='package'>")
                    .append(escape(overview.starterPackage().name()))
                    .append("</p></div><a href='/events/")
                    .append(event.id()).append("/edit'>Изменить</a></article>");
            }
            body.append("</div>");
        }
        body.append("</section></div>");
        return layout("Мероприятия", body.toString());
    }

    static String edit(Event event, String error) {
        StringBuilder body = new StringBuilder();
        body.append("<a class='back' href='/'>← К списку</a><section class='panel narrow'><h1>Изменить мероприятие</h1>")
            .append(message(error, null))
            .append("<form method='post' action='/events/").append(event.id()).append("/edit'>")
            .append("<label>Название<input name='title' maxlength='120' required value='")
            .append(escape(event.title())).append("'></label>")
            .append("<label>Дата<input name='date' type='date' required value='")
            .append(event.date()).append("'></label>")
            .append("<label>Количество мест<input name='capacity' type='number' min='1' max='5000' required value='")
            .append(event.capacity()).append("'></label>")
            .append("<label>Тип<select name='type'>").append(typeOptions(event.type()))
            .append("</select></label><p class='hint'>Мастер-класс: до 60 мест. Концерт: до 5000 мест.</p>")
            .append("<button type='submit'>Сохранить изменения</button></form></section>");
        return layout("Изменить мероприятие", body.toString());
    }

    static String notFound() {
        return layout("Не найдено", "<section class='panel narrow'><h1>Страница не найдена</h1><a href='/'>На главную</a></section>");
    }

    private static String typeOptions(EventType selected) {
        StringBuilder options = new StringBuilder();
        for (EventType type : EventType.values()) {
            options.append("<option value='").append(type.name()).append("'");
            if (type == selected) {
                options.append(" selected");
            }
            options.append('>').append(escape(type.label())).append("</option>");
        }
        return options.toString();
    }

    private static String message(String error, String notice) {
        if (error != null) {
            return "<p class='message error' role='alert'>" + escape(error) + "</p>";
        }
        if (notice != null) {
            return "<p class='message success' role='status'>" + escape(notice) + "</p>";
        }
        return "";
    }

    private static String layout(String title, String body) {
        return """
            <!doctype html>
            <html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
            <title>%s · Event Studio</title>
            <style>
              :root{font-family:system-ui,Arial,sans-serif;color:#172033;background:#f4f7fb}
              *{box-sizing:border-box}body{margin:0}main{max-width:1080px;margin:0 auto;padding:38px 22px 80px}
              .intro{margin-bottom:28px}.eyebrow{font-size:12px;letter-spacing:.16em;text-transform:uppercase;font-weight:700;color:#506b9b}
              h1{font-size:clamp(30px,5vw,44px);line-height:1.1;margin:8px 0 12px}h2{font-size:21px;margin:0 0 20px}h3{font-size:17px;margin:9px 0 5px}
              p{line-height:1.5;color:#526077}.intro p:last-child{max-width:650px}.grid{display:grid;grid-template-columns:minmax(300px,390px) 1fr;gap:20px;align-items:start}
              .panel{background:white;border:1px solid #dde4ef;border-radius:16px;padding:26px;box-shadow:0 6px 24px #2034530b}.narrow{max-width:570px;margin-top:18px}
              form{display:grid;gap:15px}label{display:grid;gap:7px;font-size:14px;font-weight:650}input,select{width:100%%;font:inherit;padding:11px 12px;border:1px solid #bdc9da;border-radius:9px;background:white;color:#172033}
              input:focus,select:focus{outline:2px solid #5175ce;outline-offset:1px}button{font:inherit;font-weight:700;border:0;border-radius:9px;padding:12px 16px;background:#284b9b;color:white;cursor:pointer}
              button:hover{background:#1d3977}.section-heading{display:flex;justify-content:space-between;align-items:baseline}.section-heading span{color:#75839a}.event-list{display:grid;gap:10px}
              .event{border:1px solid #e4e9f1;border-radius:11px;padding:16px;display:flex;justify-content:space-between;gap:15px;align-items:center}.event p{margin:0;font-size:14px}.event .package{margin-top:5px;color:#69805d}.hint{margin:-4px 0 0;font-size:12px}.event a,.back{color:#284b9b;font-weight:650;text-decoration:none}
              .pill{display:inline-block;background:#eaf0ff;color:#284b9b;font-size:12px;font-weight:700;border-radius:20px;padding:4px 9px}.empty{margin:0}.message{padding:12px 15px;border-radius:9px}.error{background:#fff0ef;color:#9c2c29}.success{background:#e9f7ed;color:#28663b}.back{display:inline-block;margin-bottom:8px}
              @media(max-width:760px){.grid{grid-template-columns:1fr}main{padding-top:25px}.panel{padding:20px}.event{align-items:flex-start}}
            </style></head><body><main>%s</main></body></html>
            """.formatted(escape(title), body);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
