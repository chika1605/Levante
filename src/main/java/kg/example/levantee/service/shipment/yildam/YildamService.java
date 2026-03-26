package kg.example.levantee.service.shipment.yildam;

import kg.example.levantee.dto.CityDto;
import kg.example.levantee.dto.shipmentDto.TariffInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YildamService {

    private record CityInfo(int code, String city, String region, String countryCode, double coefficient) {
    }

    private static final List<CityInfo> CITIES = List.of(
            new CityInfo(5444, "Бишкек", "город Бишкек", "KG", 0.5),
            new CityInfo(286, "Ош", "Ошская область", "KG", 2.5),
            new CityInfo(163, "Джалал-Абад", "Джалал-Абадская область", "KG", 2.3),
            new CityInfo(182, "Каракол", "Иссык-Кульская область", "KG", 2.0),
            new CityInfo(264, "Нарын", "Нарынская область", "KG", 1.8),
            new CityInfo(247, "Талас", "Таласская область", "KG", 1.5),
            new CityInfo(50, "Баткен", "Баткенская область", "KG", 3.0),
            new CityInfo(248, "Токмок", "Чуйская область", "KG", 1.0),
            new CityInfo(154, "Алматы", "Алматинская область", "KZ", 3.5),
            new CityInfo(155, "Астана", "Акмолинская область", "KZ", 4.0),
            new CityInfo(356, "Шымкент", "Туркестанская область", "KZ", 3.2),
            new CityInfo(195, "Тараз", "Жамбылская область", "KZ", 3.3),
            new CityInfo(270, "Москва", "Москва", "RU", 5.0),
            new CityInfo(137, "Санкт-Петербург", "Ленинградская область", "RU", 5.2),
            new CityInfo(270, "Новосибирск", "Новосибирская область", "RU", 4.5),
            new CityInfo(280, "Екатеринбург", "Свердловская область", "RU", 4.7),
            new CityInfo(501, "Ташкент", "Ташкентская область", "UZ", 3.8),
            new CityInfo(502, "Самарканд", "Самаркандская область", "UZ", 4.0),
            new CityInfo(503, "Фергана", "Ферганская область", "UZ", 3.6),
            new CityInfo(601, "Урумчи", "Синьцзян", "CN", 6.0),
            new CityInfo(602, "Кашгар", "Синьцзян", "CN", 6.5)
    );

    private static final List<TariffTemplate> TARIFFS = List.of(
            new TariffTemplate(1, "Стандарт склад-склад", "", 3, 7, 190.0),
            new TariffTemplate(2, "Стандарт дверь-дверь", "Курьер до двери", 3, 7, 290.0),
            new TariffTemplate(3, "Экспресс склад-склад", "Быстрая доставка", 1, 2, 350.0),
            new TariffTemplate(4, "Экспресс дверь-дверь", "Быстрая + курьер", 1, 2, 450.0)
    );

    public List<CityDto> searchCities(String name) {
        String query = name.trim().toLowerCase();
        return CITIES.stream()
                .filter(c -> c.city().toLowerCase().contains(query))
                .map(c -> new CityDto(c.code(), c.city(), c.region(), c.countryCode()))
                .toList();
    }

    public List<TariffInfo> getTariffs(int toCityCode) {
        double coefficient = CITIES.stream()
                .filter(c -> c.code() == toCityCode)
                .findFirst()
                .map(to -> {
                    double fromCoef = CITIES.stream()
                            .filter(c -> c.code() == 5444)
                            .findFirst().map(CityInfo::coefficient).orElse(1.0);
                    return toCityCode == 5444 ? to.coefficient() : (fromCoef + to.coefficient()) / 2.0;
                })
                .orElse(1.0);

        return TARIFFS.stream()
                .map(t -> new TariffInfo(
                        "YILDAM:" + t.code() + ":" + toCityCode,
                        "YILDAM",
                        t.code(),
                        t.name(),
                        t.description(),
                        t.periodMin(),
                        t.periodMax(),
                        Math.round(t.basePrice() * coefficient * 100.0) / 100.0,
                        "KGS"
                ))
                .toList();
    }

    private record TariffTemplate(int code, String name, String description,
                                  int periodMin, int periodMax, double basePrice) {
    }
}