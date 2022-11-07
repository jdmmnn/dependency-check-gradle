package org.owasp.dependencycheck.gradle.service

import org.owasp.dependencycheck.gradle.service.adapter.NotificationAdapter
import org.owasp.dependencycheck.gradle.service.adapter.SlackAdapter
import org.owasp.dependencycheck.gradle.service.adapter.TeamsAdapter

class NotificationSenderService {
    private final List<NotificationAdapter> adapters = []

    NotificationSenderService(settings) {
        adapters.add(new TeamsAdapter(settings))
        adapters.add(new SlackAdapter(settings))
    }

    void send(String projectName, String msg) {
        adapters.each { adapter ->
            adapter.send(projectName, msg)
        }
    }
}
