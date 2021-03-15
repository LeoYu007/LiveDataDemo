package com.yu.reinforce.task


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * 发布到蒲公英
 */
class ChannelTask extends DefaultTask {

    @Input
    String flavorName
    @Input
    String versionCode
    @Input
    String versionName
    @Input
    String buildTypeName

    @TaskAction
    void buildChannel() {
        println "开始构建渠道包"

        def apkPath = project.buildDir.absolutePath + "/channel_base.apk"
        if (!new File(apkPath).exists()) {
            println "渠道基线包不存在：apkPath = $apkPath"
            println "尝试先执行 _360JiaguRelease 任务"
            return
        }
        println "apkPath = $apkPath"

        def reinforceDir = project.rootProject.projectDir.absolutePath + "/reinforce"
        def jarPath = reinforceDir + "/vasdolly/VasDolly.jar"
        def channelPath = reinforceDir + "/vasdolly/channel.txt"
        def shellPath = reinforceDir + "/buildChannel.sh"
        def output = project.buildDir.absolutePath + "/channelApk"

        String command = 'sh ' + shellPath + ' ' + jarPath + ' ' + channelPath + ' ' + apkPath + ' ' + output

        project.exec {
            executable 'bash'
            args "-c", command
        }
        println "渠道包打包完成"

        // 重命名渠道包，添加buildType、versionName等信息

        println "开始重命名渠道包"
        new File(output).eachFile { f ->
            println "原文件名：${f.name}"
            def outName = "${flavorName}-${buildTypeName}-v${versionName}-${f.name}"
            println "输出文件名：${outName}"
            f.renameTo(new File(output, outName))
        }
        println "重命名渠道包完成"

        println "开始上传渠道包到服务器存档"

        // 上传渠道包到服务器存档
        // TODO

        println "上传完成"
    }

}