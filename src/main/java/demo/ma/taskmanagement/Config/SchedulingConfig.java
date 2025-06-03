package demo.ma.taskmanagement.Config;


import demo.ma.taskmanagement.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {

    private final TaskService taskService;

    @Scheduled(cron = "0 0 9 * * ?") // Tous les jours à 9h
    public void checkOverdueTasks() {
        log.info("Checking for overdue tasks...");
        // Logique pour envoyer des notifications pour les tâches en retard
        // Cette méthode peut être étendue pour envoyer des emails ou notifications push
    }

    @Scheduled(cron = "0 0 8 * * ?") // Tous les jours à 8h
    public void checkTasksDueSoon() {
        log.info("Checking for tasks due soon...");
        // Logique pour envoyer des rappels pour les tâches dues bientôt
    }
}
