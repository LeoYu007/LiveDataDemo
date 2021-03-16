package com.yu.reinforce.task

import com.yu.reinforce.util.Util
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * 输出渠道包
 */
class ChannelTask extends DefaultTask {

//    private static final String url_vasdolly = 'https://github.com/Tencent/VasDolly/raw/master/command/jar/VasDolly.jar'
    private static final String url_vasdolly = 'https://gitee.com/mirrors/VasDolly/raw/master/command/jar/VasDolly.jar'

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

        def jarPath = downloadVasdollyJar(project)
        def channelPath = project.projectDir.absolutePath + "/channel.txt"
        def output = project.buildDir.absolutePath + "/channelApk"

        println "渠道配置文件:$channelPath"
        println "渠道包输出目录 = $output"

        if (!new File(channelPath).exists()) {
            println "channel.txt 配置文件不存在"
            println "请在app目录下创建channel.txt，每一行一个渠道名"
            return
        }

        String command = "java -jar ${jarPath} put -c ${channelPath} ${apkPath} ${output}"
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

    private static String downloadVasdollyJar(Project project) {
        String jarPath = project.parent.rootDir.parentFile.absolutePath + File.separator + "VasDolly.jar"
        File jarFile = new File(jarPath)
        if (!jarFile.exists()) {
            println "VasDolly.jar不存在，准备开始下载"
            Util.downloadFile(url_vasdolly, jarPath)
            println "VasDolly.jar下载完成 path = $jarPath"
        }
        return jarPath
    }

}