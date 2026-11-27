# ЛАБОРАТОРНАЯ РАБОТА — РЕВЬЮ КОДА: АНАЛИЗ КОДА И РАБОТА ПО ЗАМЕЧАНИЯМ

## 1. Цель работы, вариант, ссылки

**Цель.** Освоить процедуру ревью кода: читать чужой код по чек-листу, формулировать исполнимые замечания с указанием серьёзности, исправлять собственный код по замечаниям и прослеживать каждое изменение до конкретной строки ревью.

**Вариант.** Разбор фрагмента «Формирование наряда локомотивных бригад на смену» (`ShiftPlanLegacy.java`); образец — расчёт платы за простой (`DemurrageLegacy.java`). Пара назначена: `VeronikaZmushko`.

**Исполнитель:** Козлов (GitHub `zakharenk0`), группа GI-41 (номер группы уточнить).

**Ссылки:**

| Что | Ссылка |
|---|---|
| Моё ревью PR однокурсника (7 line-comments, Request changes) | https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#pullrequestreview-6025854716 |
| PR однокурсника `VeronikaZmushko` | https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1 |
| Мой PR (в описание вставлен отчёт самопроверки) | https://github.com/vgkozlov/stations-gi41/pull/74 |
| Полученные мной ревью (Approve без замечаний) | PR [#73](https://github.com/vgkozlov/stations-gi41/pull/73#pullrequestreview-5712118516), PR [#74](https://github.com/vgkozlov/stations-gi41/pull/74#pullrequestreview-5712160550) |

**Состав файлов в папке:** `ReviewNotes.csv`, `ShiftPlanLegacy.java`, `ShiftPlanFixed.java`, `ShiftPlanFixed_output.txt`, `Traceability.md`, `GitHubReview.md`, `SelfCheckReport.md`, `selfcheck.py`, `Report.md`, заполненный `Лабораторная работа ревью_кода.ipynb` (все ячейки 3.2, 4.1, 5.1, 5.2, 6.1, 7, 8 с выводами), а также `наряд.txt` (результат работы программы).

---

## 2. Таблица замечаний к выданному фрагменту

Заполнена в `ReviewNotes.csv`: **13 записей**, из них **3 blocker** и **8 major** (всего весомых 11), 1 minor, 1 nit. Каждое замечание содержит место, последствие и предложение.

| № | Строка | Категория | Серьёзность | Замечание | Предложение |
|---|---|---|---|---|---|
| 1 | 24–27 | Корректность | blocker | Удаление элемента из списка внутри `for-each`: при первом же удалении итератор выбрасывает `ConcurrentModificationException`, наряд падает на исходных данных. | Собирать подходящие бригады в новый список, не удаляя элементы во время обхода. |
| 2 | 31–33 | Поведение при ошибках | blocker | Если `need` больше числа подходящих бригад, `brigades.get(i)` выбрасывает `IndexOutOfBoundsException` без объяснения причины. | Проверять `need` против размера отобранного списка и бросать `IllegalArgumentException` с числами «нужно / доступно». |
| 3 | 35–39 | Поведение при ошибках | blocker | Пустой `catch` проглатывает ошибку записи: наряд не сохранён, а метод возвращает строку как будто всё успешно. | Перехватывать `IOException`, бросать `IllegalStateException` с причиной и не писать успешную запись в журнал. |
| 4 | 23–26 | Проектирование | major | `make` изменяет переданный список: вызывающий код теряет бригады, повторный вызов даёт другой результат. | Работать с копией входного списка, не изменяя аргумент. |
| 5 | 36–37 | Поведение при ошибках | major | `FileWriter` не закрывается: данные могут остаться в буфере, дескриптор утекает, кодировка по умолчанию искажает кириллицу. | `try-with-resources` и явная кодировка UTF-8. |
| 6 | 9 | Корректность | major | Общий `static SimpleDateFormat` не потокобезопасен: при параллельных вызовах метки времени искажаются. | Неизменяемый `DateTimeFormatter` из `java.time`. |
| 7 | 10 | Данные и безопасность | major | Общий изменяемый `static` список `log`: записи разных запросов смешиваются, размер не ограничен. | Потокобезопасная коллекция с ограничением размера либо передача журнала параметром. |
| 8 | 41 | Корректность | major | «наряд сформирован» пишется даже при неудачной записи и при пустом наряде: журнал лжёт. | Писать запись только после успешной записи файла и только для непустого наряда. |
| 9 | 25 | Корректность | major | Проверка `hoursPerShift > 10.0` не обрабатывает `NaN`/`Infinity` и не проверяет знак: отрицательные часы попадают в наряд. | Проверять конечность и положительность, некорректные значения отклонять `IllegalArgumentException`. |
| 10 | 23 | Поведение при ошибках | major | `need <= 0` не проверяется: метод молча возвращает пустую строку и пишет файл с журналом. | Отклонять неположительное число бригад `IllegalArgumentException`. |
| 11 | 30–32 | Проектирование | minor | Конкатенация в цикле даёт O(n²) копий; завершающий `"; "` — часть текущего формата и должен сохраниться. | `StringBuilder`, сохранив завершающий разделитель. |
| 12 | 12–20 | Тесты | major | Нет характеризующего теста: ожидаемый наряд и границы не зафиксированы, исправление легко сделать несовместимым. | Добавить характеризующий тест с ожидаемой строкой и проверки ошибок. |
| 13 | 25 | Оформление | nit | Магическое число `10.0` и `admitted == false` затрудняют чтение. | Именованная константа и `!brigade.admitted`. |

### Ответ на вопрос раздела 3.2 (`static` в методе `calc`)

Замечание о `static` **не нужно**. `calc` — чистая функция: не использует состояние объекта, результат зависит только от аргументов. Для такой функции `static` — уместный и более простой вызов; перевод в метод экземпляра без необходимости создавать объект был бы вкусовым требованием без обоснования. Другое дело, если бы появилась конфигурация тарифов или стратегия расчёта — тогда потребовался бы экземпляр с зависимостями, и это было бы отдельное изменение с обоснованием.

---

## 3. Исправленный файл и вывод самопроверок

### 3.1 Фиксация исходного поведения (характеризующий тест)

Исходный файл на штатных данных падает:

```
Exception in thread "main" java.util.ConcurrentModificationException
	at ShiftPlanLegacy.make(ShiftPlanLegacy.java:24)
	at ShiftPlanLegacy.main(ShiftPlanLegacy.java:19)
```

На корректных данных (все бригады допущены, часы ≤ 10, `need = 3`) исходная версия возвращает
`"Иванов; Петров; Сидоров; "` (с завершающим `"; "`). Именно эта строка зафиксирована в самопроверке №1 до внесения правок — так гарантируется сохранение внешнего поведения при корректных входных данных.

### 3.2 Листинг `ShiftPlanFixed.java`

```java
public class ShiftPlanFixed {

    /* Номера в комментариях ссылаются на строки ReviewNotes.csv. */

    /** №13: порог смены вынесен в именованную константу. */
    private static final double MAX_HOURS_PER_SHIFT = 10.0;
    private static final String OUTPUT_FILE = "наряд.txt";
    private static final int LOG_LIMIT = 100;

    /** №7: разделяемый журнал защищён и ограничен по размеру. */
    private static final java.util.List<String> LOG =
            java.util.Collections.synchronizedList(new java.util.ArrayList<String>());

    /** №6: java.time-форматтер неизменяем и безопасен при параллельном использовании. */
    private static final java.time.format.DateTimeFormatter LOG_DATE =
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) throws InterruptedException {
        // Демонстрация на корректных данных: ожидаемый результат зафиксирован в самопроверке №1.
        java.util.List<Brigade> demo = validBrigades();
        String order = make(demo, 3);
        System.out.println(order);
        System.out.println("Журнал: " + LOG);

        // --- характеризующий тест и самопроверки (№12) ---
        check("№1  эталон: состав наряда на корректных данных",
                "Иванов; Петров; Сидоров; ".equals(make(validBrigades(), 3)));

        check("№2  нехватка бригад: осмысленное исключение",
                throwsType(IllegalArgumentException.class, "нужно 5",
                        () -> make(validBrigades(), 5)));

        check("№4  ошибка записи файла не проглатывается",
                throwsType(IllegalStateException.class, "Не удалось записать",
                        () -> make(validBrigades(), 2, missingDirectoryFile())));

        java.util.List<Brigade> source = new java.util.ArrayList<Brigade>();
        source.add(new Brigade("Иванов", 11.0, true));
        source.add(new Brigade("Петров", 8.0, false));
        source.add(new Brigade("Козлов", 8.0, true));
        check("№3  исходный список не изменяется",
                "Козлов; ".equals(make(source, 1)) && source.size() == 3
                        && "Козлов".equals(source.get(2).lastName));

        int logBeforeFailure = LOG.size();
        check("№8  журнал не пополняется при неудачной записи",
                throwsType(IllegalStateException.class, "Не удалось записать",
                        () -> make(validBrigades(), 2, missingDirectoryFile()))
                        && LOG.size() == logBeforeFailure);

        check("№9  отрицательные часы отклоняются",
                throwsType(IllegalArgumentException.class, "продолжительность",
                        () -> make(java.util.Arrays.asList(
                                new Brigade("Петров", -5.0, true),
                                new Brigade("Козлов", 8.0, true)), 1)));

        check("№9  NaN в часах отклоняется",
                throwsType(IllegalArgumentException.class, "продолжительность",
                        () -> make(java.util.Arrays.asList(
                                new Brigade("Петров", Double.NaN, true),
                                new Brigade("Козлов", 8.0, true)), 1)));

        check("№10 неположительное need отклоняется",
                throwsType(IllegalArgumentException.class, "положительным",
                        () -> make(validBrigades(), 0)));

        String written = make(validBrigades(), 3);
        check("№5  файл записан, закрыт и содержит UTF-8 наряд",
                written.equals(readFile(OUTPUT_FILE)));

        check("№6  метка времени журнала имеет формат dd.MM.yyyy",
                !LOG.isEmpty() && LOG.get(LOG.size() - 1)
                        .matches("\\d{2}\\.\\d{2}\\.\\d{4} наряд сформирован"));

        LOG.clear(); // очищаем перед проверкой ограничения журнала в 100 записей
        Thread first = new Thread(() -> writeLogEntries());
        Thread second = new Thread(() -> writeLogEntries());
        first.start();
        second.start();
        first.join();
        second.join();
        check("№6/7 параллельные обращения к журналу не теряют записи",
                LOG.size() == 100 && LOG.stream().allMatch(entry -> entry
                        .matches("\\d{2}\\.\\d{2}\\.\\d{4} наряд сформирован")));
    }

    /** Формирует наряд в файл по умолчанию. */
    public static String make(java.util.List<Brigade> brigades, int need) {
        return make(brigades, need, OUTPUT_FILE);
    }

    /**
     * №3: исходный список не изменяется; №2, №9, №10: проверка аргументов;
     * №4, №5: ошибка записи не скрывается, файл закрывается; №8: журнал пишется только после успеха.
     */
    public static String make(java.util.List<Brigade> brigades, int need, String fileName) {
        if (brigades == null) {
            throw new IllegalArgumentException("Список бригад не задан");
        }
        if (need <= 0) {
            throw new IllegalArgumentException("Число бригад в наряде должно быть положительным: " + need);
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла наряда не задано");
        }

        java.util.List<Brigade> eligible = new java.util.ArrayList<Brigade>();
        for (Brigade brigade : brigades) {
            if (brigade == null) {
                throw new IllegalArgumentException("Список содержит пустую бригаду");
            }
            brigade.validate();
            if (brigade.hoursPerShift > MAX_HOURS_PER_SHIFT || !brigade.admitted) { // №13
                continue;
            }
            eligible.add(brigade);
        }

        if (need > eligible.size()) {
            throw new IllegalArgumentException(
                    "Для наряда нужно " + need + " бригад, а допущено " + eligible.size());
        }

        // №11: StringBuilder вместо конкатенации в цикле; завершающий "; " сохранён.
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < need; i++) {
            result.append(eligible.get(i).lastName).append("; ");
        }
        String order = result.toString();

        try (java.io.Writer writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(fileName), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(order);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Не удалось записать наряд в файл " + fileName, e);
        }

        log(java.time.LocalDate.now().format(LOG_DATE) + " наряд сформирован");
        return order;
    }

    private static java.util.List<Brigade> validBrigades() {
        java.util.List<Brigade> brigades = new java.util.ArrayList<Brigade>();
        brigades.add(new Brigade("Иванов", 8.0, true));
        brigades.add(new Brigade("Петров", 9.0, true));
        brigades.add(new Brigade("Сидоров", 7.5, true));
        brigades.add(new Brigade("Козлов", 6.0, true));
        return brigades;
    }

    private static String missingDirectoryFile() {
        return System.getProperty("java.io.tmpdir") + java.io.File.separator
                + "lab8-kozlov-no-such-directory" + java.io.File.separator + "наряд.txt";
    }

    private static String readFile(String fileName) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл " + fileName, e);
        }
    }

    private static void log(String message) {
        synchronized (LOG) {
            LOG.add(message);
            while (LOG.size() > LOG_LIMIT) {
                LOG.remove(0);
            }
        }
    }

    private static void writeLogEntries() {
        for (int i = 0; i < 50; i++) {
            log(java.time.LocalDate.now().format(LOG_DATE) + " наряд сформирован");
        }
    }

    private static boolean throwsType(Class<? extends Throwable> type, String messagePart, Runnable action) {
        try {
            action.run();
            return false;
        } catch (Throwable thrown) {
            return type.isInstance(thrown)
                    && (messagePart == null || String.valueOf(thrown.getMessage()).contains(messagePart));
        }
    }

    private static void check(String name, boolean passed) {
        System.out.println((passed ? "[OK] " : "[FAIL] ") + name);
        if (!passed) {
            throw new AssertionError(name);
        }
    }
}

/** №1, №9: данные наряда; проверка корректности перенесена в метод validate. */
class Brigade {
    final String lastName;
    final double hoursPerShift;
    final boolean admitted;

    Brigade(String lastName, double hoursPerShift, boolean admitted) {
        this.lastName = lastName;
        this.hoursPerShift = hoursPerShift;
        this.admitted = admitted;
    }

    void validate() {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия бригадира не задана");
        }
        if (!Double.isFinite(hoursPerShift) || hoursPerShift <= 0.0) {
            throw new IllegalArgumentException(
                    "Некорректная продолжительность смены: " + hoursPerShift + " у " + lastName);
        }
    }
}
```

### 3.3 Вывод самопроверок

```
Иванов; Петров; Сидоров; 
Журнал: [23.11.2026 наряд сформирован]
[OK] №1  эталон: состав наряда на корректных данных
[OK] №2  нехватка бригад: осмысленное исключение
[OK] №4  ошибка записи файла не проглатывается
[OK] №3  исходный список не изменяется
[OK] №8  журнал не пополняется при неудачной записи
[OK] №9  отрицательные часы отклоняются
[OK] №9  NaN в часах отклоняется
[OK] №10 неположительное need отклоняется
[OK] №5  файл записан, закрыт и содержит UTF-8 наряд
[OK] №6  метка времени журнала имеет формат dd.MM.yyyy
[OK] №6/7 параллельные обращения к журналу не теряют записи
```

11 самопроверок, `[FAIL]` нет, завершение с кодом 0. Команда запуска (JDK 17): `java -Dfile.encoding=UTF-8 ShiftPlanFixed.java`.

---

## 4. Таблица прослеживаемости «замечание → изменение»

| № | Серьёзность | Изменение в коде |
|---|---|---|
| 1 | blocker | Вместо удаления в `for-each` собирается список `eligible`; входной список только читается. |
| 2 | blocker | `need > eligible.size()` → `IllegalArgumentException` с числами «нужно / допущено». |
| 3 | blocker | `IOException` оборачивается в `IllegalStateException`; журнал пишется только после успешной записи. |
| 4 | major | Метод не изменяет аргумент; поля `Brigade` объявлены `final`. |
| 5 | major | `try-with-resources` + `OutputStreamWriter(..., UTF_8)`. |
| 6 | major | `SimpleDateFormat` заменён на неизменяемый `DateTimeFormatter`. |
| 7 | major | `synchronizedList` + `synchronized (LOG)` + `LOG_LIMIT = 100`. |
| 8 | major | Запись в журнал после успеха и только при `need > 0`. |
| 9 | major | `Brigade.validate()`: `Double.isFinite`, положительные часы, осмысленное сообщение. |
| 10 | major | `need <= 0` → `IllegalArgumentException` до расчёта. |
| 11 | minor | `StringBuilder` с сохранением завершающего `"; "`. |
| 12 | major | 11 самопроверок, включая характеризующий тест и проверки границ. |
| 13 | nit | `MAX_HOURS_PER_SHIFT` и `!brigade.admitted`. |

Все 13 замечаний устранены; изменение кода вне таблицы не производилось.

---

## 5. Сведения о проведённом ревью

| Показатель | Значение |
|---|---|
| Оставлено замечаний | 7 (3 major, 3 minor, 1 nit), все привязаны к строкам PR `VeronikaZmushko/Lab8_Code_Review#1` |
| Решение по чуждому PR | Request changes (`CHANGES_REQUESTED`) |
| Отмечено удачных решений | 1 (разделение `buildShiftPlan`/`saveToFile`, try-with-resources, константы, инкапсуляция `Brigade`; отдельно отмечены ответы автора на прежние замечания) |
| Получено замечаний | 0 (получены только Approve от `VeronikaZmushko` на PR #73 и #74, без line-comments) |
| Принято / исправлено | 0 |
| Отклонено с обоснованием | 0 |
| Отчёт самопроверки | Вставлен в описание PR [#74](https://github.com/vgkozlov/stations-gi41/pull/74), см. `SelfCheckReport.md` |

Поскольку на мой PR содержательных замечаний не поступило, отвечать на обсуждения и нажимать *Resolve conversation* было нечего; отдельные коммиты с правками не потребовались. В `GitHubReview.md` приведён шаблон ответа для случая, когда замечания поступят.

---

## 6. Контрольные вопросы

**1. Чем ревью отличается от тестирования и статического анализа? Почему ни одно из трёх не заменяет остальные?**
Тестирование проверяет заявленное поведение на конкретных сценариях; статический анализатор ищет шаблонные дефекты по формальным правилам без запуска; ревью человеком выявляет неверно понятое требование, неудачное имя, решение, которое завтра придётся переписывать, и несоответствие духу системы. Анализатор не знает бизнес-контекст, тест не проверяет «правильность самой проверки», а ревью не гарантирует полноты прогона. Ревью дешевле исправления после релиза, но дороже автоматических проверок — поэтому они дополняют друг друга.

**2. Какие дефекты статический анализатор находит лучше человека, а какие не находит никогда?**
Лучше: разыменование `null`, сравнение строк через `==`, неиспользуемые значения, утечки ресурсов, перекрытие исключений, синхронизация и гонки по шаблону, уязвимые API, стиль. Не найдёт никогда: неверную бизнес-формулу, неверно понятое требование, выбор не той модели данных, бессмысленное, но синтаксически корректное имя, тест, который повторяет реализацию и всегда зелёный, неверный порог скидки, зафиксированный в ТЗ.

**3. Из каких трёх частей состоит полезное замечание? Пример переформулирования.**
Место (файл и строка), последствие/нарушенный принцип, предложение направления. Плохо: «А если отрицательное?» Хорошо: «Строка 19: при отрицательном числе часов метод вернёт отрицательную плату и напечатает её. Предлагаю отклонять такие данные исключением `IllegalArgumentException`». Говорим о коде, не об авторе.

**4. Что означает каждый уровень серьёзности и кто принимает решение по уровню nit?**
`blocker` — слияние невозможно; `major` — исправить в этом же запросе; `minor` — желательно исправить; `nit` — вкусовое. Уровень предлагает рецензент, но окончательное решение по `nit` остаётся за автором: он вправе не согласиться, не объясняя. Уровень «по умолчанию» из чек-листа — отправная точка, а не приговор.

**5. Почему `Approve` при незакрытом `blocker` опаснее, чем отсутствие ревью?**
При отсутствии ревью дефект остаётся видимым и его могут найти позже; одобрение создаёт ложную уверенность: запрос сливают, ответственность формально распределена, исправление откладывают, а договорённости «потом поправим» никто не отслеживает. Поэтому одобрять можно только после закрытия blocker — это защита процесса, а не формальность.

**6. Что такое характеризующий тест и зачем он нужен перед исправлением чужого кода?**
Это тест, фиксирующий текущее наблюдаемое поведение на корректных данных до рефакторинга. Он позволяет отличить намеренное изменение от случайной поломки совместимости. Здесь до правок зафиксирована строка `"Иванов; Петров; Сидоров; "`, включая завершающий `"; "`; после исправлений проверка №1 гарантирует, что формат наряда не изменился.

**7. Почему крупный PR получает менее качественное ревью, чем несколько мелких?**
Растёт объём контекста, который нужно удержать в голове, внимание рецензента истощается, замечания становятся общими, а исправления — большими и рискованными. Мелкие PR за то же время получают больше содержательных комментариев и быстрее закрываются.

**8. Какие дефекты были заложены в фрагменте расчёта платы за простой и какие видны только при чтении?**
Дефекты: сравнение строк через `==`, отрицательная плата при часах меньше нормы, дублирование формулы, три ветвления по типу вагона, магические числа 24 / 1.15 / 1000 / 0.9, печать внутри расчётного метода, `double` для денег, отсутствие тестов. При запуске видны разве что отрицательная плата и вывод на консоль; остальное — сравнение ссылок, дублирование формулы, нерасширяемость, непрослеживаемые константы и побочный вывод — проявляется только при чтении кода.

**9. Почему сравнение строк через `==` срабатывает при запуске, но является дефектом?**
Литералы `"крытый"`, `"цистерна"` попадают в пул строк, поэтому ссылки совпадают и условие работает на демонстрационных данных. Как только тип вагона придёт из файла, базы или сети (`new String(...)`), ссылки будут разными и расчёт молча уйдёт в ветку «иначе». Нужны `equals` или, лучше, `enum`.

**10. Какую роль играет файл `CODEOWNERS` и обязательное ревью перед слиянием?**
`CODEOWNERS` назначает ответственных рецензентов по путям репозитория: GitHub автоматически запрашивает их одобрение. Обязательное ревью в защите ветки запрещает слияние без одобрения и закрытых обсуждений, делает ответственность явной и не даёт одному человеку единолично менять критичные части системы. Вместе они превращают ревью из случайной привычки в воспроизводимый контроль качества.

---

## 7. Разбор и выводы

| Вопрос | Ответ |
|---|---|
| Сколько замечаний и сколько blocker? | 13 замечаний, 3 blocker, 8 major, 1 minor, 1 nit. |
| Какое замечание найдено не сразу? | Ложная запись в журнал при неудаче записи (строка 41) и разделяемый `SimpleDateFormat` (строка 9): на одиночном запуске они не проявляются, их удалось увидеть только по чек-листу «Поведение при ошибках» и «Данные и безопасность». |
| Было ли вкусовое выдано за существенное? | Нет. Единственное вкусовое требование помечено `nit`; у остальных указано конкретное последствие. |
| Какое полученное замечание отклонено? | Не было: на мой PR замечаний не поступило (Approve без комментариев). |

**Три вывода.**

1. «Запускается — значит работает» — опасная иллюзия: исходный фрагмент падал на собственных данных, а образец расчёта платы давал правдоподобные числа при сравнении строк через `==` и отрицательной плате.
2. Полезное замечание — это не оценка, а три части: место, последствие, направление исправления; серьёзность превращает его в исполнимый контракт, а `nit` остаётся правом автора не согласиться.
3. Характеризующий тест до правок и таблица прослеживаемости после них превращают ревью из обсуждения в управляемый процесс: каждое изменение объяснено, а совместимость на корректных данных проверена.
