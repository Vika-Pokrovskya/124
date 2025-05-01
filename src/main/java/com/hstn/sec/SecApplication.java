package com.hstn.sec;

import com.hstn.sec.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class  SecApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecApplication.class, args);

		//UserService userService = context.getBean(UserService.class);
		//userService.migrateOldPasswords();
	}

}

