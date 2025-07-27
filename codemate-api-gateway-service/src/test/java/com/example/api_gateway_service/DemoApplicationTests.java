import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient // ✅ for WebFlux
@ActiveProfiles("test")     // ✅ optional profile isolation
class DemoApplicationTests {
    @Test
    void contextLoads() {
    }
}
