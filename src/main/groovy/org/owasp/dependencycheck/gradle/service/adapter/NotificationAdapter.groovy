package org.owasp.dependencycheck.gradle.service.adapter

interface NotificationAdapter {

    abstract def send(String projectName, String msg)

}