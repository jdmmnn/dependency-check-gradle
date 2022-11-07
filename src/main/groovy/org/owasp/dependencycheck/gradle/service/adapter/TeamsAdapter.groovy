package org.owasp.dependencycheck.gradle.service.adapter

import com.google.common.base.Preconditions
import com.mashape.unirest.http.Unirest
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TeamsAdapter implements NotificationAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeamsAdapter.class)
    public static final String TEAMS__WEBHOOK__ENABLED = "TEAMS_WEBHOOK_ENABLED"
    public static final String TEAMS__WEBHOOK__URL = "TEAMS_WEBHOOK_URL"

    private boolean enabled = false
    private String webhookUrl

    TeamsAdapter(def settings) {
        def enabled = settings.getBoolean(TEAMS__WEBHOOK__ENABLED)
        def webhookUrl = settings.getString(TEAMS__WEBHOOK__URL)
        if (enabled) {
            Preconditions.checkArgument(StringUtils.isNotBlank(webhookUrl), "a teams webhook url is required")
            this.webhookUrl = webhookUrl
            this.enabled = true
        }
    }

    @Override
    def send(String projectName, String msg) {
        if (enabled) {
            def response = Unirest.post(webhookUrl)
                    .header("Content-Type", "application/json")
                    .body("{\"text\": \"Security issues found in *$projectName*\", \"attachments\": [{\"color\": \"danger\", \"text\": \"$msg\", \"fallback\": \"$msg\"}]}").asString()

            if (response.getStatus() != 200) {
                LOGGER.error("Failed to send teams notification: ${response.getStatusText()}")
            }
            LOGGER.info("Send dependency-check notification to Teams.")
        }
    }
}
