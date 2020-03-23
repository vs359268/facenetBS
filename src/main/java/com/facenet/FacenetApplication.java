package com.facenet;

import com.facenet.utils.FilePath;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ServletComponentScan
@SpringBootApplication
@EnableJpaAuditing
public class FacenetApplication {

	static {
		System.load(FilePath.getResource("lib/opencv_java340.dll"));//加载OpenCV
	}
	public static void main(String[] args) {
		SpringApplication.run(FacenetApplication.class, args);
	}

}
