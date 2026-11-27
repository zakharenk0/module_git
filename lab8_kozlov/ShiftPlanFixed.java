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
