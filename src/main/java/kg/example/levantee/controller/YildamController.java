package kg.example.levantee.controller;

import kg.example.levantee.dto.CityDto;
import kg.example.levantee.service.shipment.yildam.YildamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/yildam")
@RequiredArgsConstructor
public class YildamController {

    private final YildamService yildamService;

    @GetMapping("/cities")
    public ResponseEntity<List<CityDto>> searchCities(@RequestParam String name) {
        return ResponseEntity.ok(yildamService.searchCities(name));
    }
}