package com.yu.reinforce.task

import com.yu.reinforce.util.PublishUtil
import com.yu.reinforce.util.Util
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * 发布到蒲公英
 */
class PublishApkTask extends DefaultTask {

    /*
     * 安装密码，若为空则不需要密码
     */
    @Input
    String password
    /*
     * 有效期，单位天，如果小于等于0则长期有效
     */
    @Input
    int deadTime
    @Input
    String apiKey

    @Input
    String dingTalkUrl
    @Input
    String dingTalkSec

    @Input
    String buildTypeName
    @Input
    String apkPath

    @TaskAction
    void publishToPgy() {
        println "开始执行发布到蒲公英task"

        if (Util.isEmpty(apkPath)) {
            println "未找到待发布的apk"
            return
        }
        if (Util.isEmpty(apiKey)) {
            println "蒲公英api为空"
            return
        }

        def message = PublishUtil.uploadToPgy(apiKey, apkPath, password, deadTime, buildTypeName)
        if (message != null) {
            PublishUtil.sendMessageToDingDing(project, dingTalkUrl, dingTalkSec, message)
        }

    }

}