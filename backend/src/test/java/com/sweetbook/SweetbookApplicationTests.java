package com.sweetbook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:contexttest;MODE=MySQL;DB_CLOSE_DELAY=-1",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.flyway.locations=classpath:db/migration",
	"app.upload-dir=./build/contexttest-uploads",
	"app.ai.mock-mode=true"
})
class SweetbookApplicationTests {

	@Test
	void contextLoads() {
	}

}
