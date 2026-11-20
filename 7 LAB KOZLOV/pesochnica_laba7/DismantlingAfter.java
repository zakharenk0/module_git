// Вариант 7, сортировочная станция. Код «после»:
// Strategy — очерёдность расформирования состава.
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DismantlingAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        List<TrainCar> cars = sampleCars();
        List<String> originalOrder = carNumbers(cars);

        // Каталог стратегий: план расформирования не знает, какие правила существуют.
        Map<String, DismantlingOrderStrategy> strategyCatalog = new LinkedHashMap<>();
        strategyCatalog.put("BY_DESTINATION", new ByDestinationOrder());
        strategyCatalog.put("BY_WEIGHT", new ByWeightOrder());
        strategyCatalog.put("BY_TYPE", new ByCarTypeOrder());

        DismantlingPlan dismantlingPlan = new DismantlingPlan();

        System.out.println("--- Очерёдность расформирования по стратегиям ---");
        for (Map.Entry<String, DismantlingOrderStrategy> catalogEntry : strategyCatalog.entrySet()) {
            List<String> order = dismantlingPlan.makeOrder(cars, catalogEntry.getValue());
            System.out.printf("%-22s %-30s %s%n",
                    catalogEntry.getKey(), catalogEntry.getValue().title(), String.join(" → ", order));
        }

        System.out.println();
        System.out.println("--- Характеризующие тесты: поведение совпадает с кодом «до» ---");
        check("порядок по назначению совпадает с эталоном «до»",
                dismantlingPlan.makeOrder(cars, strategyCatalog.get("BY_DESTINATION"))
                        .equals(legacyOrder(cars, "BY_DESTINATION")));
        check("порядок по массе совпадает с эталоном «до»",
                dismantlingPlan.makeOrder(cars, strategyCatalog.get("BY_WEIGHT"))
                        .equals(legacyOrder(cars, "BY_WEIGHT")));
        check("порядок по типу вагона совпадает с эталоном «до»",
                dismantlingPlan.makeOrder(cars, strategyCatalog.get("BY_TYPE"))
                        .equals(legacyOrder(cars, "BY_TYPE")));
        check("в коде «до» новое правило оставляло исходный порядок",
                legacyOrder(cars, "BY_PRIORITY").equals(originalOrder));
        check("разные стратегии дают разную очерёдность",
                !dismantlingPlan.makeOrder(cars, strategyCatalog.get("BY_DESTINATION"))
                        .equals(dismantlingPlan.makeOrder(cars, strategyCatalog.get("BY_WEIGHT"))));

        System.out.println();
        System.out.println("--- Тест открытости/закрытости: новая очерёдность без правки плана ---");
        DismantlingOrderStrategy priorityOrder = new DismantlingOrderStrategy() {
            public List<String> order(List<TrainCar> carsToOrder) {
                return sortedNumbers(carsToOrder, Comparator.comparing((TrainCar car) -> car.priority)
                        .reversed().thenComparing((TrainCar car) -> car.number));
            }
            public String title() {
                return "сначала приоритетные вагоны";
            }
        };
        List<String> priorityOrderResult = dismantlingPlan.makeOrder(cars, priorityOrder);
        System.out.printf("%-22s %-30s %s%n", "BY_PRIORITY", priorityOrder.title(),
                String.join(" → ", priorityOrderResult));
        check("новая стратегия изменила порядок, DismantlingPlan не изменялся",
                !priorityOrderResult.equals(originalOrder));
        check("новая стратегия не потеряла ни одного вагона",
                priorityOrderResult.size() == originalOrder.size()
                        && priorityOrderResult.containsAll(originalOrder));

        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }

    static List<String> carNumbers(List<TrainCar> cars) {
        List<String> numbers = new ArrayList<>();
        for (TrainCar car : cars) {
            numbers.add(car.number);
        }
        return numbers;
    }

    static List<String> sortedNumbers(List<TrainCar> cars, Comparator<TrainCar> comparator) {
        List<TrainCar> orderedCars = new ArrayList<>(cars);
        orderedCars.sort(comparator);
        return carNumbers(orderedCars);
    }

    // Эталон старого поведения для характеризующих тестов.
    static List<String> legacyOrder(List<TrainCar> cars, String rule) {
        List<TrainCar> orderedCars = new ArrayList<>(cars);
        if (rule.equals("BY_DESTINATION")) {
            orderedCars.sort(Comparator.comparing((TrainCar car) -> car.destination)
                    .thenComparing((TrainCar car) -> car.number));
        } else if (rule.equals("BY_WEIGHT")) {
            orderedCars.sort(Comparator.comparing((TrainCar car) -> car.weightTons).reversed()
                    .thenComparing((TrainCar car) -> car.number));
        } else if (rule.equals("BY_TYPE")) {
            orderedCars.sort(Comparator.comparing((TrainCar car) -> car.carType)
                    .thenComparing((TrainCar car) -> car.number));
        }
        return carNumbers(orderedCars);
    }

    static List<TrainCar> sampleCars() {
        List<TrainCar> cars = new ArrayList<>();
        cars.add(new TrainCar("1", "Москва", 70, "крытый", false));
        cars.add(new TrainCar("2", "Брест", 55, "цистерна", true));
        cars.add(new TrainCar("3", "Москва", 62, "платформа", false));
        cars.add(new TrainCar("4", "Киев", 48, "крытый", false));
        cars.add(new TrainCar("5", "Брест", 75, "цистерна", false));
        return cars;
    }
}

/** Стратегия: правило очерёдности расформирования. */
interface DismantlingOrderStrategy {
    List<String> order(List<TrainCar> cars);
    String title();
}

/** Контекст: строит порядок расформирования, не зная перечня правил. */
class DismantlingPlan {
    List<String> makeOrder(List<TrainCar> cars, DismantlingOrderStrategy strategy) {
        return strategy.order(cars);
    }
}

class ByDestinationOrder implements DismantlingOrderStrategy {
    public List<String> order(List<TrainCar> cars) {
        return DismantlingAfter.sortedNumbers(cars,
                Comparator.comparing((TrainCar car) -> car.destination)
                        .thenComparing((TrainCar car) -> car.number));
    }
    public String title() {
        return "сначала по назначению";
    }
}

class ByWeightOrder implements DismantlingOrderStrategy {
    public List<String> order(List<TrainCar> cars) {
        return DismantlingAfter.sortedNumbers(cars,
                Comparator.comparing((TrainCar car) -> car.weightTons).reversed()
                        .thenComparing((TrainCar car) -> car.number));
    }
    public String title() {
        return "сначала тяжёлые вагоны";
    }
}

class ByCarTypeOrder implements DismantlingOrderStrategy {
    public List<String> order(List<TrainCar> cars) {
        return DismantlingAfter.sortedNumbers(cars,
                Comparator.comparing((TrainCar car) -> car.carType)
                        .thenComparing((TrainCar car) -> car.number));
    }
    public String title() {
        return "сначала по типу вагона";
    }
}

/** Вагон состава: номер, направление, масса, тип и признак приоритетности. */
class TrainCar {
    final String number;
    final String destination;
    final int weightTons;
    final String carType;
    final boolean priority;

    TrainCar(String number, String destination, int weightTons, String carType, boolean priority) {
        this.number = number;
        this.destination = destination;
        this.weightTons = weightTons;
        this.carType = carType;
        this.priority = priority;
    }
}
