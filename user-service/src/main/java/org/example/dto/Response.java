package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

@Schema(description = "API с HATEOAS")
public class Response extends RepresentationModel<Response> {

    @Schema(description = "Сообщение ответа", example = "Операция выполнена успешно")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Статус операции", example = "SUCCESS")
    @JsonProperty("status")
    private String status;

    public Response() {
    }

    public Response(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
