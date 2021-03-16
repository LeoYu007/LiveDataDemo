package com.yu.reinforce.task

import com.android.builder.model.SigningConfig
import com.yu.reinforce.util.JiaguUtil
import com.yu.reinforce.util.SignUtil
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
    String outputApk

    @TaskAction
    void doReinforceAndSign() {
        println "开始执行360加固task"

        // 删除上次的临时文件
        String outputDir = cleanDir()

        // 执行加固，失败后最多重试3次
        String reinforcedApk = JiaguUtil.reinforce(project, apkPath, outputDir, account, password)
        // 加固后执行重新签名
        if (Util.isNotEmpty(reinforcedApk)) {
            println "加固成功，正在执行签名"
            String signedApk = SignUtil.signApk(project, signingConfig, reinforcedApk)

            // 拷贝加固的apk到build目录下，方便多渠道打包
            project.copy {
                from signedApk
                into project.buildDir.absolutePath
                rename {
                    "channel_base.apk"
                }
            }

            // 重命名apk
            new File(signedApk).renameTo(new File(outputApk))

            println "拷贝加固后的apk到channel_base完成"
        } else {
            println '加固失败，未找到360加固的apk!'
        }
    }

    private String cleanDir() {
        // 加固输出目录
        String outputDir = "${new File(apkPath).getParentFile().absolutePath}${File.separator}jiagu"
        // 上次的渠道基线包
        String oldApkPath = "${project.buildDir.absolutePath}${File.separator}channel_base.apk"
        // 渠道包输出目录
        String channelDir = "${project.buildDir.absolutePath}${File.separator}channelApk"

        project.delete {
            delete outputDir, oldApkPath, channelDir
        }

        File out = new File(outputDir)
        if (!out.exists()) {
            out.mkdirs()
        }
        return outputDir
    }

}