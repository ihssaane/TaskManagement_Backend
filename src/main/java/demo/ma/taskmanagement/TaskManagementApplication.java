package demo.ma.taskmanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagementApplication {

	public static void main(String[] args) {
		// Load .env file
		try {
			Dotenv dotenv = Dotenv.configure().load();
			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue())
			);
		} catch (Exception e) {
			System.out.println("No .env file found, using system properties");
		}

		SpringApplication.run(TaskManagementApplication.class, args);
	}
}