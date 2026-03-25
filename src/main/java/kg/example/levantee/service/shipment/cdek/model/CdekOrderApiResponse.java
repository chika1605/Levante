package kg.example.levantee.service.shipment.cdek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CdekOrderApiResponse {

    private Entity entity;
    private List<Request> requests;

    @Data
    public static class Entity {
        private String uuid;

        @JsonProperty("cdek_number")
        private String cdekNumber;
    }

    @Data
    public static class Request {
        private String state;
        private List<Error> errors;

        @Data
        public static class Error {
            private String code;
            private String message;
        }
    }
}