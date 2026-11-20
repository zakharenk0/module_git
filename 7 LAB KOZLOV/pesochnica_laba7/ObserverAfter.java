// Вариант 7, сортировочная станция. Код «после»:
// Observer — событие «состав расформирован» получают подписчики.
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObserverAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        Station station = new Station("Гомель-Сортировочная");

        BoardDisplay boardDisplay = new BoardDisplay();
        DismantlingJournal dismantlingJournal = new DismantlingJournal();
        NotificationService notificationService = new NotificationService();
        AsuIntegration asuIntegration = new AsuIntegration();   // четвёртый наблюдатель

        station.addListener(boardDisplay);
        station.addListener(dismantlingJournal);
        station.addListener(notificationService);

        System.out.println("--- Расформирование состава 3022: подписаны три наблюдателя ---");
        station.trainDismantled("3022", 62);
        List<String> firstDeliveryOrder = station.deliveryOrder();
        printOrder("Порядок получения события (1-е событие)", firstDeliveryOrder);
        System.out.println("Записей в журнале: " + dismantlingJournal.recordCount());

        System.out.println();
        System.out.println("--- Проверки Observer (вариант 7) ---");
        check("после одного события журнал получил ровно одну запись",
                dismantlingJournal.recordCount() == 1);
        check("все подписчики получили одно и то же событие",
                boardDisplay.lastEvent().equals("Гомель-Сортировочная|3022|62")
                        && notificationService.lastEvent().equals("Гомель-Сортировочная|3022|62")
                        && dismantlingJournal.records().get(0).contains("3022"));

        String boardEventBeforeUnsubscribe = boardDisplay.lastEvent();

        // Отписка и подключение четвёртого наблюдателя: класс Station не меняется.
        station.removeListener(boardDisplay);
        station.addListener(asuIntegration);

        System.out.println();
        System.out.println("--- Расформирование состава 6701: табло отписано, подключена АСУ ---");
        station.trainDismantled("6701", 48);
        printOrder("Порядок получения события (2-е событие)", station.deliveryOrder());
        System.out.println("Записей в журнале: " + dismantlingJournal.recordCount());

        check("после отписки табло больше не получает события",
                boardDisplay.lastEvent().equals(boardEventBeforeUnsubscribe)
                        && boardDisplay.receiveCount() == 1);
        check("четвёртый наблюдатель (АСУ) получил событие без правки Station",
                asuIntegration.receiveCount() == 1
                        && asuIntegration.lastEvent().equals("Гомель-Сортировочная|6701|48"));
        check("журнал накопил обе записи, оставаясь подписанным",
                dismantlingJournal.recordCount() == 2
                        && dismantlingJournal.records().get(1).contains("6701"));
        check("порядок первого события: табло → журнал → рассылка",
                firstDeliveryOrder.equals(Arrays.asList("табло", "журнал", "рассылка")));
        check("порядок второго события: журнал → рассылка → АСУ",
                station.deliveryOrder().equals(Arrays.asList("журнал", "рассылка", "АСУ")));

        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }

    static void printOrder(String caption, List<String> deliveryOrder) {
        System.out.println(caption + ": " + String.join(" → ", deliveryOrder));
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }
}

/** Наблюдатель: получает событие «состав расформирован». */
interface DismantlingListener {
    void onTrainDismantled(String stationName, String trainNumber, int carCount);
    String listenerTitle();
}

/** Субъект: сортировочная станция. Не знает, какие именно наблюдатели существуют. */
class Station {

    private final String stationName;
    private final List<DismantlingListener> listeners = new ArrayList<>();
    private final List<String> currentDeliveryOrder = new ArrayList<>();

    Station(String stationName) {
        this.stationName = stationName;
    }

    void addListener(DismantlingListener listener) {
        listeners.add(listener);
    }

    void removeListener(DismantlingListener listener) {
        listeners.remove(listener);
    }

    void trainDismantled(String trainNumber, int carCount) {
        System.out.println("Событие: состав расформирован — станция " + stationName
                + ", поезд " + trainNumber + ", вагонов " + carCount);
        currentDeliveryOrder.clear();
        for (DismantlingListener listener : listeners) {
            listener.onTrainDismantled(stationName, trainNumber, carCount);
            currentDeliveryOrder.add(listener.listenerTitle());
        }
    }

    List<String> deliveryOrder() {
        return new ArrayList<>(currentDeliveryOrder);
    }
}

class BoardDisplay implements DismantlingListener {
    private String lastStationName = "";
    private String lastTrainNumber = "";
    private int lastCarCount = 0;
    private int receiveCount = 0;

    public void onTrainDismantled(String stationName, String trainNumber, int carCount) {
        lastStationName = stationName;
        lastTrainNumber = trainNumber;
        lastCarCount = carCount;
        receiveCount++;
        System.out.println("[табло]  Состав " + trainNumber + " расформирован на станции "
                + stationName + " (" + carCount + " вагонов)");
    }
    public String listenerTitle() {
        return "табло";
    }
    String lastEvent() {
        return lastStationName + "|" + lastTrainNumber + "|" + lastCarCount;
    }
    int receiveCount() {
        return receiveCount;
    }
}

class DismantlingJournal implements DismantlingListener {
    private final List<String> records = new ArrayList<>();

    public void onTrainDismantled(String stationName, String trainNumber, int carCount) {
        String record = stationName + ": состав " + trainNumber
                + " расформирован (" + carCount + " вагонов)";
        records.add(record);
        System.out.println("[журнал] " + record);
    }
    public String listenerTitle() {
        return "журнал";
    }
    List<String> records() {
        return new ArrayList<>(records);
    }
    int recordCount() {
        return records.size();
    }
}

class NotificationService implements DismantlingListener {
    private String lastStationName = "";
    private String lastTrainNumber = "";
    private int lastCarCount = 0;

    public void onTrainDismantled(String stationName, String trainNumber, int carCount) {
        lastStationName = stationName;
        lastTrainNumber = trainNumber;
        lastCarCount = carCount;
        System.out.println("[рассылка] Оповещение: состав " + trainNumber
                + " расформирован на станции " + stationName);
    }
    public String listenerTitle() {
        return "рассылка";
    }
    String lastEvent() {
        return lastStationName + "|" + lastTrainNumber + "|" + lastCarCount;
    }
}

class AsuIntegration implements DismantlingListener {
    private String lastStationName = "";
    private String lastTrainNumber = "";
    private int lastCarCount = 0;
    private int receiveCount = 0;

    public void onTrainDismantled(String stationName, String trainNumber, int carCount) {
        lastStationName = stationName;
        lastTrainNumber = trainNumber;
        lastCarCount = carCount;
        receiveCount++;
        System.out.println("[АСУ]     Передача в АСУ: станция " + stationName
                + ", состав " + trainNumber + " расформирован, вагонов " + carCount);
    }
    public String listenerTitle() {
        return "АСУ";
    }
    String lastEvent() {
        return lastStationName + "|" + lastTrainNumber + "|" + lastCarCount;
    }
    int receiveCount() {
        return receiveCount;
    }
}
