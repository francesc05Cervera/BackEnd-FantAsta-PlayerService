package com.example.fantasta.player_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:service-test",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "auth.service.url=http://localhost:7070"
})
class PlayerServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
