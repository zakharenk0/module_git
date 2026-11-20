// Код «до» (вариант 7, сортировочная станция):
// источник события жёстко связан со всеми получателями.
public class ObserverBefore {

    public static void main(String[] args) {
        Station station = new Station("Гомель-Сортировочная");
        station.trainDismantled("3022", 62);
        station.trainDismantled("6701", 48);
    }
}

class Station {

    private final String stationName;

    Station(String stationName) {
        this.stationName = stationName;
    }

    void trainDismantled(String trainNumber, int carCount) {
        // Любой новый получатель требует правки этого метода
        System.out.println("[табло]  Состав " + trainNumber + " расформирован на станции "
                + stationName + " (" + carCount + " вагонов)");
        System.out.println("[журнал] " + stationName + ": зафиксировано расформирование состава "
                + trainNumber + ", вагонов " + carCount);
    }
}
