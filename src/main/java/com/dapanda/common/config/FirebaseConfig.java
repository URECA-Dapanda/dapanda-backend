package com.dapanda.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirebaseConfig {

	@PostConstruct
	public void initialize() {
		try {
			InputStream serviceAccount = getClass().getClassLoader()
					.getResourceAsStream("firebase/firebase-service-account.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
				log.info("FirebaseApp 초기화 완료");
			}
		} catch (Exception e) {
			log.error("Firebase 초기화 실패", e);
		}
	}
}
