import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@WireMockTest
@Slf4j
class BasicServerTest {

    @Test
    void simpleStubTesting(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, InterruptedException {
        String responseBody = "Hello World !!";
        String apiUrl = "/api-url";

        // Stub
        WireMock.stubFor(WireMock.get(apiUrl).willReturn(WireMock.ok(responseBody)));

        String apiResponse = getContent(wmRuntimeInfo.getHttpBaseUrl() + apiUrl);
        Assertions.assertEquals(apiResponse, responseBody);

        //Verify API is hit
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(apiUrl)));
    }

    @Test
    void simpleJsonTesting(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, InterruptedException {
        User user = new User(123, "John Doe", "john@example.com");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(user);

        String apiUrl = "/api-url";

        // Stub
        WireMock.stubFor(WireMock.get(apiUrl)
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)
                )
        );

        String apiResponse = getContent(wmRuntimeInfo.getHttpBaseUrl() + apiUrl);
        Assertions.assertEquals(apiResponse, jsonResponse);

        //Verify API is hit
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(apiUrl)));
    }

    private String getContent(String url) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
    }
}
