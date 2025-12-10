package searchengine.dto.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private boolean result = true;
    private Integer count;
    private List<?> data;

    public static ApiResponse ok() {
        return new ApiResponse();
    }

    public static ApiResponse error(String errorMessage) {
        return new ApiResponseError(errorMessage);
    }

    @Getter
    private static class ApiResponseError extends ApiResponse {
        private final String error;

        public ApiResponseError(String errorMessage) {
            super.setResult(false);
            this.error = errorMessage;
        }
    }
}


