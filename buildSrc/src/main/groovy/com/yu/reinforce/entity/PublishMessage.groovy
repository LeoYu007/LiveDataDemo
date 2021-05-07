package com.yu.reinforce.entity


class PublishMessage {

    String appName
    String appVersion
    String appUrl
    String qrCodeUrl
    String branchName
    String buildType
    String password
    String date
    String pgyBuildVersion

    PublishMessage(String appName, String appVersion, String appUrl, String qrCodeUrl, String branchName, String buildType, String password, String date, String pgyBuildVersion) {
        this.appName = appName
        this.appVersion = appVersion
        this.appUrl = appUrl
        this.qrCodeUrl = qrCodeUrl
        this.branchName = branchName
        this.buildType = buildType
        this.password = password
        this.date = date
        this.pgyBuildVersion = pgyBuildVersion
    }

}