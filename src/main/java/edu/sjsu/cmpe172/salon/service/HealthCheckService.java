package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.dto.HealthCheckResponse;
import edu.sjsu.cmpe172.salon.enums.OperationalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;

@Service
public class HealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);

    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final NotificationGatewayService notificationGatewayService;

    public HealthCheckService(@Value("${salon.db.url}") String dbUrl,
                              @Value("${salon.db.username}") String dbUsername,
                              @Value("${salon.db.password}") String dbPassword,
                              NotificationGatewayService notificationGatewayService) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.notificationGatewayService = notificationGatewayService;
    }

    public HealthCheckResponse checkHealth() {
        OperationalStatus databaseStatus = checkDatabase();
        OperationalStatus notificationStatus = OperationalStatus.UNKNOWN;

        if (databaseStatus == OperationalStatus.UP) {
            notificationStatus = notificationGatewayService.isHealthy()
                    ? OperationalStatus.UP
                    : OperationalStatus.DOWN;
        }

        OperationalStatus status = resolveOverallStatus(databaseStatus, notificationStatus);
        return new HealthCheckResponse(
                status,
                "salon-booking",
                databaseStatus,
                notificationStatus,
                Instant.now());
    }

    private OperationalStatus checkDatabase() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            if (connection.isValid(2)) {
                return OperationalStatus.UP;
            }

            logger.error("health_check_dependency_failed dependency=database status=DOWN reason=\"connection invalid\"");
            return OperationalStatus.DOWN;
        } catch (SQLException ex) {
            logger.error("health_check_dependency_failed dependency=database status=DOWN errorType={} message=\"{}\"",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            return OperationalStatus.DOWN;
        }
    }

    private OperationalStatus resolveOverallStatus(OperationalStatus databaseStatus, OperationalStatus notificationStatus) {
        if (databaseStatus != OperationalStatus.UP) {
            return OperationalStatus.DOWN;
        }
        if (notificationStatus != OperationalStatus.UP) {
            return OperationalStatus.DEGRADED;
        }
        return OperationalStatus.UP;
    }
}
