package org.owasp.dependencycheck.gradle.service.adapter

interface NotificationAdapter {

    def send(String projectName, String msg)

}