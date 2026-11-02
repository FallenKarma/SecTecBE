package it.sectec.platform.installation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GoogleGeocodingService {
    private final String apiKey;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final JsonMapper mapper = JsonMapper.builder().build();
    public GoogleGeocodingService(@Value("${app.geocoding.google-api-key:}") String apiKey) { this.apiKey = apiKey; }

    public Optional<Coordinates> locate(String indirizzo, String cap, String comune) {
        if (apiKey.isBlank()) return Optional.empty();
        var address = String.join(", ", indirizzo, cap, comune);
        var uri = URI.create("https://maps.googleapis.com/maps/api/geocode/json?address="
                + URLEncoder.encode(address, StandardCharsets.UTF_8) + "&language=it&region=it&key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        try {
            var result = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString());
            return result.statusCode() == 200 ? parse(result.body()) : Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            // Never log the URL, API key, address or Google's response.
            return Optional.empty();
        }
    }

    Optional<Coordinates> parse(String body) {
        try {
            var json = mapper.readTree(body);
            var results = json.path("results");
            if (!"OK".equals(json.path("status").asText()) || results.size() != 1) return Optional.empty();
            var result = results.get(0);
            if (result.path("partial_match").asBoolean(false)) return Optional.empty();
            var location = result.path("geometry").path("location");
            var lat = location.path("lat");
            var lng = location.path("lng");
            if (!lat.isNumber() || !lng.isNumber()) return Optional.empty();
            var latitude = lat.decimalValue();
            var longitude = lng.decimalValue();
            if (latitude.abs().compareTo(BigDecimal.valueOf(90)) > 0 || longitude.abs().compareTo(BigDecimal.valueOf(180)) > 0) return Optional.empty();
            return Optional.of(new Coordinates(latitude.setScale(6, RoundingMode.HALF_UP), longitude.setScale(6, RoundingMode.HALF_UP)));
        } catch (RuntimeException e) { return Optional.empty(); }
    }
    public record Coordinates(BigDecimal latitude, BigDecimal longitude) {}
}
