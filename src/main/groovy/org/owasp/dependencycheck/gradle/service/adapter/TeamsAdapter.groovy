package org.owasp.dependencycheck.gradle.service.adapter

import com.google.common.base.Preconditions
import com.mashape.unirest.http.Unirest
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.json.Json

class TeamsAdapter implements NotificationAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeamsAdapter.class)
    public static final String TEAMS__WEBHOOK__ENABLED = "TEAMS_WEBHOOK_ENABLED"
    public static final String TEAMS__WEBHOOK__URL = "TEAMS_WEBHOOK_URL"
    public static final String TEAMS__WEBHOOK__MENTION__USER__EMAIL = "TEAMS_WEBHOOK_MENTION_EMAIL"
    public static final String TEAMS__WEBHOOK__MENTION__USER__NAME = "TEAMS_WEBHOOK_MENTION_NAME"


    private boolean enabled = false
    private String webhookUrl
    private String mentionUserEmail
    private String mentionUserName

    TeamsAdapter(def settings) {
        enabled = settings.getBoolean(TEAMS__WEBHOOK__ENABLED)
        webhookUrl = settings.getString(TEAMS__WEBHOOK__URL)
        mentionUserEmail = settings.getString(TEAMS__WEBHOOK__MENTION__USER__EMAIL)
        mentionUserName = settings.getString(TEAMS__WEBHOOK__MENTION__USER__NAME)
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
                    .body(createMessage(projectName, msg))

            if (response.getStatus() != 200) {
                LOGGER.error("Failed to send dependency-check notification to teams: ${response.getStatusText()}")
            }
            LOGGER.info("Send dependency-check notification to Teams.")
        }
    }

    private String createMessage(String projectName, String msg) {
        def message = Json.createObjectBuilder()
                .add("type", "message")
                .add("attachments", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("contentType", "application/vnd.microsoft.card.adaptive")
                                .add("content", Json.createObjectBuilder()
                                        .add("type", "AdaptiveCard")
                                        .add("version", "1.3")
                                        .add("body", Json.createArrayBuilder()
                                                .add(Json.createObjectBuilder()
                                                        .add("type", "TextBlock")
                                                        .add("text", "Security issues found in *$projectName*")
                                                        .add("size", "large")
                                                        .add("weight", "bolder")
                                                        .build()
                                                )
                                                .add(Json.createObjectBuilder()
                                                        .add("type", "TextBlock")
                                                        .add("text", msg)
                                                        .add("wrap", true)
                                                        .build()
                                                )
                                                .add(Json.createObjectBuilder()
                                                        .add("type", "TextBlock")
                                                        .add("text", "<at>$mentionUserName</at>")
                                                        .build()
                                                ).build()
                                        ).build()
                                )
                                .add("msteams", Json.createObjectBuilder()
                                        .add("width", "full")
                                        .add("entities", Json.createArrayBuilder()
                                                .add(Json.createObjectBuilder()
                                                        .add("type", "mention")
                                                        .add("text", "<at>$mentionUserName</at>")
                                                        .add("mentioned", Json.createObjectBuilder()
                                                                .add("id", mentionUserEmail)
                                                                .add("name", "$mentionUserName")
                                                                .build()
                                                        ).build()
                                                ).build()
                                        ).build()
                                ).build()
                        ).build()
                ).build()
        return message.toString()
    }
}
