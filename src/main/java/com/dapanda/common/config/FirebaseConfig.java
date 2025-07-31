package com.dapanda.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirebaseConfig {

	@Bean
	public FirebaseApp firebaseApp() {
		try {
			InputStream serviceAccount = getClass().getClassLoader()
					.getResourceAsStream("firebase/firebase-service-account.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
				log.info("FirebaseApp 초기화 완료");
				return firebaseApp;
			} else {
				return FirebaseApp.getInstance();
			}

		} catch (Exception e) {
			log.error("FirebaseApp 초기화 실패", e);
			throw new IllegalStateException("Firebase 초기화 실패", e);
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
		return FirebaseMessaging.getInstance(firebaseApp);
	}
}
