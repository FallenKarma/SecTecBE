package it.sectec.platform.installation.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class GoogleGeocodingServiceTests {
    private final GoogleGeocodingService service = new GoogleGeocodingService("");
    @Test void roundsValidCoordinatesAndDoesNotCallGoogleWithoutKey() {
        var coordinates = service.parse("""
            {"status":"OK","results":[{"geometry":{"location":{"lat":41.1234567,"lng":-12.1234567}}}]}
            """).orElseThrow();
        assertThat(coordinates.latitude()).isEqualByComparingTo("41.123457");
        assertThat(coordinates.longitude()).isEqualByComparingTo("-12.123457");
        assertThat(service.locate("Via Roma 1", "00100", "Roma")).isEmpty();
    }
    @Test void rejectsErrorsAmbiguityPartialMatchesAndInvalidCoordinates() {
        for (String body : new String[] {
            "invalid", "{}", "{\"status\":\"ZERO_RESULTS\",\"results\":[]}",
            "{\"status\":\"REQUEST_DENIED\"}", "{\"status\":\"OVER_QUERY_LIMIT\"}",
            "{\"status\":\"OK\",\"results\":[{},{}]}",
            "{\"status\":\"OK\",\"results\":[{\"partial_match\":true}]}",
            "{\"status\":\"OK\",\"results\":[{\"geometry\":{\"location\":{\"lat\":91,\"lng\":12}}}]}",
            "{\"status\":\"OK\",\"results\":[{\"geometry\":{\"location\":{\"lat\":41}}}]}"
        }) assertThat(service.parse(body)).as(body).isEmpty();
    }
}
