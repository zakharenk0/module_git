// Код «до» (вариант 7, сортировочная станция):
// очерёдность расформирования выбирается цепочкой условных операторов.
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DismantlingBefore {

    public static void main(String[] args) {
        List<TrainCar> cars = sampleCars();
        printOrder("по назначению", legacyOrder(cars, "BY_DESTINATION"));
        printOrder("по массе", legacyOrder(cars, "BY_WEIGHT"));
        printOrder("по типу вагона", legacyOrder(cars, "BY_TYPE"));
        printOrder("новое правило (приоритет)", legacyOrder(cars, "BY_PRIORITY"));
    }

    static void printOrder(String ruleName, List<String> carNumbers) {
        System.out.println(ruleName + ": " + String.join(" → ", carNumbers));
    }

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
        // Про новое правило метод не знает: порядок остаётся исходным.
        List<String> carNumbers = new ArrayList<>();
        for (TrainCar car : orderedCars) {
            carNumbers.add(car.number);
        }
        return carNumbers;
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
