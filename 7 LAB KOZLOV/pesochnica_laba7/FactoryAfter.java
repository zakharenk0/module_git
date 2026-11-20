// Вариант 7, сортировочная станция. Код «после»:
// Factory Method — фабрика создаёт тип отцепа.
import java.util.Arrays;
import java.util.List;

public class FactoryAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        // Вызывающий код работает только с типами OtsepCreator и Otsep.
        // Четвёртый создатель добавлен новым классом, старые классы не тронуты.
        List<OtsepCreator> otsepCreators = Arrays.asList(
                new DestinationOtsepCreator(),
                new TransitOtsepCreator(),
                new RepairOtsepCreator(),
                new LocalOtsepCreator());

        System.out.println("--- Отцепы, созданные фабричным методом ---");
        for (OtsepCreator otsepCreator : otsepCreators) {
            Otsep otsep = otsepCreator.createOtsep(4, 68.0);
            System.out.printf("%-24s %-32s %d вагонов, %.1f т%n",
                    otsepCreator.title(), otsep.description(), otsep.carCount(), otsep.capacityTons());
        }

        Otsep destinationOtsep = otsepCreators.get(0).createOtsep(4, 68.0);
        Otsep transitOtsep = otsepCreators.get(1).createOtsep(5, 71.0);
        Otsep repairOtsep = otsepCreators.get(2).createOtsep(2, 66.0);
        Otsep localOtsep = otsepCreators.get(3).createOtsep(3, 70.0);

        System.out.println();
        System.out.println("--- Проверки Factory Method (вариант 7) ---");
        check("описание отцепа по назначению не пустое и содержит число вагонов",
                !destinationOtsep.description().isEmpty()
                        && destinationOtsep.description().contains("4"));
        check("описание содержит общую грузоподъёмность отцепа",
                destinationOtsep.description().contains("272.0")
                        && destinationOtsep.capacityTons() == 272.0);
        check("созданный объект имеет тип DestinationOtsep",
                destinationOtsep instanceof DestinationOtsep);
        check("созданный объект имеет тип TransitOtsep",
                transitOtsep instanceof TransitOtsep);
        check("созданный объект имеет тип RepairOtsep",
                repairOtsep instanceof RepairOtsep);
        check("четвёртый тип (местный отцеп) работает наравне с остальными",
                localOtsep instanceof LocalOtsep
                        && localOtsep.description().contains("Местн")
                        && localOtsep.carCount() == 3);
        check("фабричный метод создаёт новый объект при каждом вызове",
                otsepCreators.get(0).createOtsep(4, 68.0)
                        != otsepCreators.get(0).createOtsep(4, 68.0));
        check("все создатели дают непустые описания",
                allDescriptionsAreNotEmpty(otsepCreators));
        check("код «до» не знал про местный отцеп (характеризующий тест)",
                legacyDescribe("LOCAL", 3, 70.0).startsWith("Неизвестный"));

        finishChecks();
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }

    // Проверку завершения выносим из main: в main нет никаких ветвлений по типу.
    static void finishChecks() {
        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
    }

    static boolean allDescriptionsAreNotEmpty(List<OtsepCreator> otsepCreators) {
        for (OtsepCreator otsepCreator : otsepCreators) {
            if (otsepCreator.createOtsep(1, 60.0).description().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Старая реализация сохранена только как эталон поведения для характеризующего теста.
    static String legacyDescribe(String otsepType, int carCount, double capacityPerCarTons) {
        double totalCapacityTons = carCount * capacityPerCarTons;
        if (otsepType.equals("BY_DESTINATION")) {
            return "Отцеп по назначению: " + carCount + " вагонов, грузоподъёмность "
                    + totalCapacityTons + " т, следует на сортировочный путь по направлению";
        } else if (otsepType.equals("TRANSIT")) {
            return "Транзитный отцеп: " + carCount + " вагонов, грузоподъёмность "
                    + totalCapacityTons + " т, следует без переработки";
        } else if (otsepType.equals("REPAIR")) {
            return "Ремонтный отцеп: " + carCount + " вагонов, грузоподъёмность "
                    + totalCapacityTons + " т, вагоны направлены в вагонное депо";
        }
        return "Неизвестный тип отцепа: " + otsepType;
    }
}

/** Продукт: отцеп — группа вагонов, отделяемая от состава на сортировочной станции. */
interface Otsep {
    String description();
    int carCount();
    double capacityTons();
}

/** Создатель: фабричный метод возвращает отцеп нужного типа. */
abstract class OtsepCreator {
    abstract Otsep createOtsep(int carCount, double capacityPerCarTons);
    abstract String title();
}

class DestinationOtsep implements Otsep {
    private final int carCount;
    private final double capacityPerCarTons;

    DestinationOtsep(int carCount, double capacityPerCarTons) {
        this.carCount = carCount;
        this.capacityPerCarTons = capacityPerCarTons;
    }
    public String description() {
        return "Отцеп по назначению: " + carCount + " вагонов, грузоподъёмность "
                + capacityTons() + " т, следует на сортировочный путь по направлению";
    }
    public int carCount() {
        return carCount;
    }
    public double capacityTons() {
        return carCount * capacityPerCarTons;
    }
}

class TransitOtsep implements Otsep {
    private final int carCount;
    private final double capacityPerCarTons;

    TransitOtsep(int carCount, double capacityPerCarTons) {
        this.carCount = carCount;
        this.capacityPerCarTons = capacityPerCarTons;
    }
    public String description() {
        return "Транзитный отцеп: " + carCount + " вагонов, грузоподъёмность "
                + capacityTons() + " т, следует без переработки";
    }
    public int carCount() {
        return carCount;
    }
    public double capacityTons() {
        return carCount * capacityPerCarTons;
    }
}

class RepairOtsep implements Otsep {
    private final int carCount;
    private final double capacityPerCarTons;

    RepairOtsep(int carCount, double capacityPerCarTons) {
        this.carCount = carCount;
        this.capacityPerCarTons = capacityPerCarTons;
    }
    public String description() {
        return "Ремонтный отцеп: " + carCount + " вагонов, грузоподъёмность "
                + capacityTons() + " т, вагоны направлены в вагонное депо";
    }
    public int carCount() {
        return carCount;
    }
    public double capacityTons() {
        return carCount * capacityPerCarTons;
    }
}

class LocalOtsep implements Otsep {
    private final int carCount;
    private final double capacityPerCarTons;

    LocalOtsep(int carCount, double capacityPerCarTons) {
        this.carCount = carCount;
        this.capacityPerCarTons = capacityPerCarTons;
    }
    public String description() {
        return "Местный отцеп: " + carCount + " вагонов, грузоподъёмность "
                + capacityTons() + " т, подача на местный грузовой фронт";
    }
    public int carCount() {
        return carCount;
    }
    public double capacityTons() {
        return carCount * capacityPerCarTons;
    }
}

class DestinationOtsepCreator extends OtsepCreator {
    Otsep createOtsep(int carCount, double capacityPerCarTons) {
        return new DestinationOtsep(carCount, capacityPerCarTons);
    }
    String title() {
        return "создатель отцепа по назначению";
    }
}

class TransitOtsepCreator extends OtsepCreator {
    Otsep createOtsep(int carCount, double capacityPerCarTons) {
        return new TransitOtsep(carCount, capacityPerCarTons);
    }
    String title() {
        return "создатель транзитного отцепа";
    }
}

class RepairOtsepCreator extends OtsepCreator {
    Otsep createOtsep(int carCount, double capacityPerCarTons) {
        return new RepairOtsep(carCount, capacityPerCarTons);
    }
    String title() {
        return "создатель ремонтного отцепа";
    }
}

class LocalOtsepCreator extends OtsepCreator {
    Otsep createOtsep(int carCount, double capacityPerCarTons) {
        return new LocalOtsep(carCount, capacityPerCarTons);
    }
    String title() {
        return "создатель местного отцепа";
    }
}
