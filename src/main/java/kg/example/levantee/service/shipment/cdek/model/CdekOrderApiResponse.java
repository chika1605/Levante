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

        private List<Status> statuses;

        @Data
        public static class Status {
            private String code;
            private String name;
        }

        public String getCurrentStatusName() {
            if (statuses == null || statuses.isEmpty()) return null;
            return statuses.get(0).getName();
        }
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