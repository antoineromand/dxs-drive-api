package com.dxs.DriveProject;

import com.dxs.DriveProject.config.AbstractMongoDBTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DriveProjectApplicationTests extends AbstractMongoDBTest {

	@Test
	void contextLoads() {
	}

}
