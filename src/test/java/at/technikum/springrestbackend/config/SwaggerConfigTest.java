package at.technikum.springrestbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SwaggerConfigTest {

    @Test
    void openApi_shouldContainExpectedSecuritySchemes() {
        SwaggerConfig config = new SwaggerConfig();

        OpenAPI openAPI = config.openAPI();

        assertNotNull(openAPI.getComponents());
        SecurityScheme bearer = openAPI.getComponents().getSecuritySchemes().get("Bearer Token");
        SecurityScheme cookie = openAPI.getComponents().getSecuritySchemes().get("API Cookie");

        assertEquals(SecurityScheme.Type.HTTP, bearer.getType());
        assertEquals("bearer", bearer.getScheme());
        assertEquals("JWT", bearer.getBearerFormat());

        assertEquals(SecurityScheme.Type.APIKEY, cookie.getType());
        assertEquals(SecurityScheme.In.COOKIE, cookie.getIn());
        assertEquals("JWT", cookie.getName());
    }
}
