package com.yu.reinforce.task

import com.android.builder.model.SigningConfig
import com.yu.reinforce.util.Util
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * 360加固并签名
 */
class ReinforceTask360 extends DefaultTask {

    @Input
    SigningConfig signingConfig
    @Input
    String apkPath
    @Input
    String account
    @Input
    String password

    @Input
    String outputPath

    @TaskAction
    void doReinforceAndSign() {
        println "开始执行360加固task"

        def reinforceDir = project.rootProject.projectDir.absolutePath + "/reinforce"
        def reinforceJarPath = reinforceDir + "/jiagu/jiagu.jar"
        def reinforceShellPath = reinforceDir + "/reinforce.sh"
        def signJarPath = reinforceDir + "/signature/apksigner.jar"
        def signShellPath = reinforceDir + "/apksigner.sh"

        String outputDir = cleanDir()

        // 执行加固，失败后最多重试3次
        String reinforcedApk = reinforce(reinforceShellPath, outputDir, reinforceJarPath)

        // 加固后执行重新签名
        if (Util.isNotEmpty(reinforcedApk)) {
            println "加固成功，正在执行签名"
            signApk(reinforcedApk, signShellPath, signJarPath)

            // 拷贝加固的apk到VasDolly指定的目录下，方便多渠道打包
            // 注意VasDolly配置的baseReleaseApk必须和这里一样，否则会找不到
            project.copy {
                from outputPath
                into project.buildDir.absolutePath
                rename {
                    "channel_base.apk"
                }
            }
        } else {
            println '加固失败，未找到360加固的apk!'
        }
    }

    private String cleanDir() {
        // 加固输出目录
        String outputDir = "${new File(apkPath).getParentFile().absolutePath}/jiagu"
        // 上次的渠道基线包
        String oldApkPath = "${project.buildDir.absolutePath}/channel_base.apk"
        // 渠道包输出目录
        String channelDir = "${project.buildDir.absolutePath}/rebuildChannel"

        project.delete {
            delete outputDir, oldApkPath, channelDir
        }

        File out = new File(outputDir)
        if (!out.exists()) {
            out.mkdirs()
        }
        return outputDir
    }

    private String reinforce(String reinforceShellPath, String outputDir, String reinforceJarPath) {
        println '开始执行360加固'
        println "apkPath = $apkPath"
        println "outputDir = $outputDir"

        def rCommand = 'sh ' + reinforceShellPath + ' ' + account + ' ' + password + ' ' + apkPath + ' ' + outputDir + ' ' + reinforceJarPath
        def retryCount = 0

        while (retryCount < 3) {
            def result = project.exec {
                executable 'bash'
                args "-c", rCommand
            }

            if (result.getExitValue() == 0) {
                println "加固完成"
                break
            } else {
                println "加固出错，3秒后重试第${retryCount + 1}次"
            }

            try {
                Thread.sleep(3000)
            } catch (Exception e) {
            }

            retryCount++
        }

        def reinforcedApk = new File(outputDir).listFiles().find {
            it.getName().contains("apk")
        }.absolutePath

        return reinforcedApk
    }

    private void signApk(String apk, String signShellPath, String signJarPath) {
        println "开始签名，apk = $apk"

        def keystore = signingConfig.storeFile.absolutePath
        def storePass = signingConfig.storePassword
        def alias = signingConfig.keyAlias
        def keypass = signingConfig.keyPassword

        if (Util.isEmpty(outputPath)) {
            outputPath = apkPath.substring(0, apkPath.lastIndexOf('.'))
                    .replace("unsigned", "") + "_signed.apk"
        }

        String command = 'sh ' + signShellPath + ' ' + keystore + ' ' + alias + ' ' + storePass + ' ' + keypass + ' ' + outputPath + ' ' + apk + ' ' + signJarPath
        project.exec {
            executable 'bash'
            args "-c", command
        }
        println "签名完毕，outputPath = $outputPath"
    }

}