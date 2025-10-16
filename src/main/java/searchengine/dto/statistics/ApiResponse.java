package searchengine.dto.statistics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
    private boolean result = true;

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


