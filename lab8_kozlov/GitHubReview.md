# Ревью на GitHub (часть 3)

## Как рецензент

- **Чей запрос:** PR [#1 «feat: add code review lab8»](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1), автор `VeronikaZmushko`.
- **Ссылка на ревью:** https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#pullrequestreview-6025854716
- **Решение:** `CHANGES_REQUESTED` (Request changes).
- **Замечаний оставлено:** 7 — все привязаны к строкам, серьёзность указана префиксом; 3 major, 3 minor, 1 nit.
- **Отмеченное удачное решение:** логика разделена на `buildShiftPlan` (формирование строки) и `saveToFile` (запись), `FileWriter` закрывается через try-with-resources, пороги вынесены в константы, класс `Brigade` инкапсулирован с валидацией. Отдельно отмечено, что автор ответила на прежние замечания Violaveta и действительно внесла правки.

| № | Серьёзность | Файл:строка | Краткое содержание | Ссылка |
|---|---|---|---|---|
| 1 | major | `ShiftPlanFixed.java:120` | `hoursPerShift < 0` не отклоняет `NaN` и `Infinity`: `NaN` проходит фильтр и попадает в наряд. Нужны `Double.isFinite` и строго положительное значение. | [comment](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#discussion_r4669119665) |
| 2 | major | `ShiftPlanFixed.java:95` | `new FileWriter("наряд.txt")` пишет в системной кодировке и по пути, зависящему от каталога запуска. Нужны явная UTF-8 и путь параметром. | [comment](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#discussion_r4669147168) |
| 3 | major | `ShiftPlanFixed.java:32` | Это не characterize-тест: исходная версия на этих входных данных падает с `ConcurrentModificationException`. Ожидание нужно получить запуском legacy на корректных данных. | [comment](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#discussion_r4669119680) |
| 4 | minor | `ShiftPlanFixed.java:25` | `Петров` создан с `0.0` часами и попадает в наряд; предложено требовать строго положительную продолжительность. | [comment](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#discussion_r4669119689) |
| 5 | minor | `ShiftPlanFixed.java:75` | `MAX_SHIFT_HOURS + EPSILON` расширяет допуск; предложено сравнивать строго или задокументировать отклонение. | [comment](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#discussion_r4669119704) |
| 6 | minor | `ShiftPlanFixed.java:63` | Журнал потокобезопасен, но не ограничен: предложено ввести предел размера. | [comment](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#discussion_r4669119719) |
| 7 | nit | `ReviewNotes.csv:6` | Номер строки в таблице не совпадает с объявлением `SimpleDateFormat`; предложено поправить прослеживаемость. | [comment](https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#discussion_r4669119726) |

> Замечание №2 было повторно отправлено с исправленной формулировкой после опечатки в первом тексте: https://github.com/VeronikaZmushko/Lab8_Code_Review/pull/1#pullrequestreview-6025887750. Итоговое число line-comments — 7.

## Как автор

- **Свой запрос:** PR [#74 «test(registry): расширить тесты метода farthest (#29)»](https://github.com/vgkozlov/stations-gi41/pull/74), автор аккаунта `zakharenk0`.
- **Получено ревью:** 2 — Approve от `VeronikaZmushko` на PR [#73](https://github.com/vgkozlov/stations-gi41/pull/73#pullrequestreview-5712118516) и PR [#74](https://github.com/vgkozlov/stations-gi41/pull/74#pullrequestreview-5712160550), **без замечаний, привязанных к строкам**.
- **Замечаний получено:** 0 → принято 0, обоснованно отклонено 0. Отвечать на несуществующие обсуждения не требуется; кнопку *Resolve conversation* нажимать нечего.
- **Правки отдельными коммитами:** не потребовались, так как содержательных замечаний к моим PR не поступило.

> Если преподаватель назначит пару с обменом замечаниями на другом PR, шаблон ответа:
> «Принято и исправлено: коммит `<hash>`, строка `<...>`. Обсуждение закрываю через Resolve conversation.»
> либо «Обоснованный отказ: <причина, почему текущее поведение соответствует требованию>».

**Отчёт самопроверки** (`SelfCheckReport.md`) вставлен в описание PR [#74](https://github.com/vgkozlov/stations-gi41/pull/74).

**Итог по части 3:** оставлено 7, получено 0, принято 0, отклонено 0.
