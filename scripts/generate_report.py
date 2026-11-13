"""Generate a clearly labeled progress PDF; requires reportlab."""

from pathlib import Path

from reportlab.lib.colors import HexColor
from reportlab.lib.pagesizes import A4
from reportlab.lib.utils import ImageReader, simpleSplit
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "output" / "pdf" / "Event_Studio_Progress_Report.pdf"
FONT_PATH = Path("C:/Windows/Fonts/arial.ttf")
BOLD_PATH = Path("C:/Windows/Fonts/arialbd.ttf")
if not FONT_PATH.exists():
    raise SystemExit("Arial Cyrillic font is required to regenerate this report.")
pdfmetrics.registerFont(TTFont("Arial", str(FONT_PATH)))
pdfmetrics.registerFont(TTFont("ArialBold", str(BOLD_PATH)))

W, H = A4
INK = HexColor("#172033")
BLUE = HexColor("#284b9b")
MUTED = HexColor("#526077")
LIGHT = HexColor("#eef3fa")
BORDER = HexColor("#dce5f1")


def line_text(pdf, value, x, y, size=10, bold=False, color=INK):
    pdf.setFillColor(color)
    pdf.setFont("ArialBold" if bold else "Arial", size)
    pdf.drawString(x, y, value)


def wrap(pdf, value, x, y, width, size=10, leading=15, color=INK):
    for row in simpleSplit(value, "Arial", size, width):
        line_text(pdf, row, x, y, size=size, color=color)
        y -= leading
    return y


def page(pdf, number, title):
    pdf.setFillColor(BLUE)
    pdf.rect(0, H - 14, W, 14, stroke=0, fill=1)
    line_text(pdf, "EVENT STUDIO  /  SOFTWARE DESIGN PATTERNS", 48, H - 46, 8, True, BLUE)
    line_text(pdf, title, 48, H - 82, 21, True)
    pdf.setStrokeColor(BORDER)
    pdf.line(48, 49, W - 48, 49)
    line_text(pdf, "Черновик, 13.11.2026  •  github.com/capnco0ks/software-design-patterns-final-project",
              48, 35, 7.5, color=MUTED)
    line_text(pdf, str(number), W - 56, 35, 8, color=MUTED)


def box(pdf, x, y, width, height, title, subtitle):
    pdf.setFillColor(LIGHT)
    pdf.setStrokeColor(BORDER)
    pdf.roundRect(x, y, width, height, 8, stroke=1, fill=1)
    line_text(pdf, title, x + 9, y + height - 18, 9, True, BLUE)
    wrap(pdf, subtitle, x + 9, y + height - 35, width - 18, 7.5, 11, MUTED)


def arrow(pdf, x1, y1, x2, y2):
    pdf.setStrokeColor(BLUE)
    pdf.setLineWidth(1.2)
    pdf.line(x1, y1, x2, y2)
    import math
    angle = math.atan2(y2 - y1, x2 - x1)
    length = 5
    pdf.line(x2, y2, x2 - length * math.cos(angle - 0.5), y2 - length * math.sin(angle - 0.5))
    pdf.line(x2, y2, x2 - length * math.cos(angle + 0.5), y2 - length * math.sin(angle + 0.5))


OUTPUT.parent.mkdir(parents=True, exist_ok=True)
pdf = canvas.Canvas(str(OUTPUT), pagesize=A4, pageCompression=1)
pdf.setTitle("Event Studio - progress report")
pdf.setAuthor("Event Studio team; AI-assisted draft requested by Asanali")

# Page 1: product and actual screenshot.
page(pdf, 1, "Проект и текущий результат")
y = wrap(pdf, "Event Studio - локальное приложение на Java 21 с браузерным GUI для мероприятий, пакетов услуг и бронирований.",
         48, 725, W - 96, 11, 17)
y = wrap(pdf, "Сейчас работают создание и изменение мероприятия, бронь с двумя способами расчёта, реакция на смену статуса, импорт CSV, сводка и сохранение между запусками.",
         48, y - 8, W - 96, 10, 15)
line_text(pdf, "Главный экран после импорта и подтверждения брони", 48, 657, 10, True, BLUE)
shot = ImageReader(str(ROOT / "docs/screenshots/event-studio-home.png"))
pdf.drawImage(shot, 100, 105, width=395, height=521, preserveAspectRatio=True, anchor="c")
pdf.showPage()

# Page 2: UML class view.
page(pdf, 2, "UML: классы и паттерны")
wrap(pdf, "Стрелки показывают основные зависимости. Полная Mermaid-версия находится в docs/UML.md.",
     48, 730, W - 96, 9, 14, MUTED)
box(pdf, 206, 646, 184, 55, "EventController", "HTTP и HTML; без правил цены")
box(pdf, 206, 551, 184, 60, "EventPlanningFacade", "Собирает пользовательские сценарии")
arrow(pdf, 298, 646, 298, 611)
for x, title, detail in [
    (48, "EventCatalog", "Выбирает EventCreator"),
    (218, "BookingService", "Бронь и смена статуса"),
    (388, "ReportService", "Обходит сущности"),
]:
    box(pdf, x, 429, 155, 74, title, detail)
arrow(pdf, 258, 551, 125, 503)
arrow(pdf, 298, 551, 295, 503)
arrow(pdf, 339, 551, 465, 503)
box(pdf, 48, 297, 155, 79, "Factory Method", "WorkshopEventCreator / ConcertEventCreator")
box(pdf, 218, 297, 155, 79, "Strategy + Observer", "PricingStrategy / BookingEventLog")
box(pdf, 388, 297, 155, 79, "Visitor", "SummaryReportVisitor")
arrow(pdf, 125, 429, 125, 376)
arrow(pdf, 295, 429, 295, 376)
arrow(pdf, 465, 429, 465, 376)
box(pdf, 48, 156, 155, 82, "Adapter", "CsvBookingAdapter переводит CSV в BookingImportRow")
box(pdf, 218, 156, 155, 82, "FileDataStore", "Event, ServicePackage, Participant, Booking")
box(pdf, 388, 156, 155, 82, "Domain", "Event / ServicePackage / Participant / Booking")
arrow(pdf, 295, 297, 295, 238)
wrap(pdf, "Builder и Decorator для настройки пакета - следующий этап Бакытжана. Они должны изменить базовую цену до вызова PricingStrategy.",
     48, 125, W - 96, 9, 14, MUTED)
pdf.showPage()

# Page 3: sequence diagram.
page(pdf, 3, "UML: последовательность бронирования")
names = ["GUI", "Facade", "Booking", "Strategy", "Store", "Observer", "Report"]
xs = [61, 138, 215, 292, 369, 446, 523]
for name, x in zip(names, xs):
    pdf.setFillColor(LIGHT)
    pdf.setStrokeColor(BORDER)
    pdf.roundRect(x - 31, 663, 62, 31, 5, stroke=1, fill=1)
    line_text(pdf, name, x - pdfmetrics.stringWidth(name, "ArialBold", 8) / 2, 675, 8, True, BLUE)
    pdf.setDash(2, 3)
    pdf.line(x, 655, x, 242)
    pdf.setDash()

def message(source, target, y, label):
    arrow(pdf, xs[source], y, xs[target], y)
    mid = (xs[source] + xs[target]) / 2
    width = pdfmetrics.stringWidth(label, "Arial", 7)
    line_text(pdf, label, mid - width / 2, y + 7, 7)

message(0, 1, 623, "createBooking")
message(1, 2, 584, "create")
message(2, 3, 545, "calculate")
message(2, 4, 506, "save")
message(0, 1, 467, "changeStatus")
message(1, 2, 428, "changeStatus")
message(2, 4, 389, "save")
message(2, 5, 350, "statusChanged")
message(5, 4, 311, "appendLog")
message(0, 6, 272, "snapshot / CSV")
wrap(pdf, "При создании Strategy рассчитывает итоговую цену. При подтверждении Observer фиксирует событие. ReportService применяет Visitor к Event, ServicePackage и Booking.",
     48, 207, W - 96, 10, 16)
pdf.showPage()

# Page 4: evidence.
page(pdf, 4, "Проверки и итоговая сводка")
line_text(pdf, "Автоматические проверки: 31 сценарий прошёл", 48, 727, 11, True, BLUE)
rows = [
    "Factory Method: мастер-класс 61 место - ошибка; концерт 61 место - создан.",
    "Strategy: стандарт 5000 ₸, сервисный сбор 5500 ₸.",
    "Observer: NEW → CONFIRMED даёт одну запись; повтор запрещён.",
    "Граница: второе подтверждение при вместимости 1 отклонено.",
    "Хранение: после перезапуска есть бронь, участник, журнал и сумма.",
    "Adapter: CSV со строкой импортирован; неверный пакет отклонён.",
    "Visitor: сводка считает мероприятия, пакеты, брони и выручку.",
]
y = 695
for row in rows:
    y = wrap(pdf, "• " + row, 55, y, W - 110, 9, 14) - 5
line_text(pdf, "Фактический экран сводки после перезапуска", 48, 480, 10, True, BLUE)
shot = ImageReader(str(ROOT / "docs/screenshots/event-studio-report.png"))
pdf.drawImage(shot, 60, 126, width=475, height=330, preserveAspectRatio=True, anchor="c")
pdf.showPage()

# Page 5: contribution disclosure and next steps.
page(pdf, 5, "Вклад, ограничения и защита")
y = 725
for title, detail in [
    ("Асанали - Factory Method, Facade", "Техническая реализация подготовлена ИИ по запросу Асанали; личная переработка требуется."),
    ("Амир - Strategy, Observer", "Текущий код создан ИИ по запросу Асанали и закоммичен под существующим аккаунтом capnco0ks, не Амиром."),
    ("Рустам - Adapter, Visitor, хранение", "Текущий код и этот PDF созданы ИИ по запросу Асанали, не Рустамом."),
    ("Бакытжан - Builder, Decorator", "Его этап ещё не выполнен; для полного пути нужна настройка пакета через GUI."),
]:
    line_text(pdf, title, 48, y, 10.5, True, BLUE)
    y = wrap(pdf, detail, 48, y - 20, W - 96, 9.5, 15, MUTED) - 20
line_text(pdf, "Открытые вопросы перед сдачей", 48, y - 4, 11, True)
y -= 29
for item in [
    "Каждый студент должен лично переработать и объяснить два назначенных паттерна.",
    "Интегрировать Builder и Decorator Бакытжана; повторить полный путь и обновить диаграммы.",
    "Записать реальные результаты защиты/демонстрации и окончательный вклад участников.",
    "Файловые записи отдельных шагов не являются единой транзакцией; сбой диска возможен между ними.",
]:
    y = wrap(pdf, "• " + item, 55, y, W - 110, 9.5, 15) - 7
wrap(pdf, "Этот документ - честный технический черновик, а не подтверждение самостоятельного выполнения студентами. Исходный код и полный список тестов доступны в GitHub и docs/TEST_CASES.md.",
     48, 180, W - 96, 10, 16, MUTED)
pdf.showPage()
pdf.save()
print(OUTPUT)
