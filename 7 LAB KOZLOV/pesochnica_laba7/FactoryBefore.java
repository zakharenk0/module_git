// Код «до» (вариант 7, сортировочная станция):
// тип отцепа разбирается условным оператором внутри одного метода.
public class FactoryBefore {

    public static void main(String[] args) {
        System.out.println(describe("BY_DESTINATION", 8, 68));
        System.out.println(describe("TRANSIT", 5, 71));
        System.out.println(describe("REPAIR", 3, 66));
        System.out.println(describe("LOCAL", 4, 70));   // новый тип — обработчика нет
    }

    static String describe(String otsepType, int carCount, double capacityPerCarTons) {
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
