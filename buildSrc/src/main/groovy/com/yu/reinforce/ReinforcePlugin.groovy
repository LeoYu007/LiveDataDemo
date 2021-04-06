package com.yu.reinforce

import com.yu.reinforce.extension.PgyExtension
import com.yu.reinforce.extension.ReinforceExtension360
import com.yu.reinforce.task.ChannelTask
import com.yu.reinforce.task.PublishApkTask
import com.yu.reinforce.task.ReinforceTask360
import com.yu.reinforce.util.Util
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ReinforcePlugin implements Plugin<Project> {

    private Project project
    private ReinforceExtension360 m360Extension
    private PgyExtension mPgyExtension

    @Override
    void apply(Project project) {
        this.project = project
        m360Extension = project.extensions.create("reinforceConfig360", ReinforceExtension360)
        mPgyExtension = project.extensions.create("pgyConfig", PgyExtension)

        project.afterEvaluate {
            project.android.applicationVariants.all { variant ->
                def flavorName = variant.flavorName
                def versionCode = variant.versionCode
                def versionName = variant.versionName
                def buildTypeName = variant.buildType.getName()
                def signConfig = variant.getSigningConfig()

                def assembleTask = project.tasks.find {
                    it.name == "assemble${Util.toUpperFirstChar(flavorName)}${Util.toUpperFirstChar(buildTypeName)}"
                }

                variant.outputs.each {
                    String apkPath = it.getOutputFile().absolutePath
                    String jiaguOutput = it.getOutputFile().getParentFile().absolutePath + File.separator + "jiagu" + File.separator + "temp_jiagu_signed.apk"
//                    println "待加固apk path = ${apkPath}"
//                    println "加固后输出apk path = ${jiaguOutput}"
                    if ("debug".equalsIgnoreCase(buildTypeName)) {
                        def debugApkPath = project.buildDir.absolutePath + File.separator + "outputs" + File.separator + "apk" + File.separator + "debug" + File.separator + "app-debug.apk"
                        createPublishApkTask("build${Util.toUpperFirstChar(buildTypeName)}Apk", assembleTask, debugApkPath, buildTypeName)
                        createPublishApkTask("build${Util.toUpperFirstChar(buildTypeName)}ApkOnly", null, debugApkPath, buildTypeName)
                    } else {
                        // 创建task，分为依赖assemble和不依赖两种
                        // 360加固
                        Task _360Task = create360Task(signConfig, assembleTask, apkPath, jiaguOutput)

                        createPublishApkTask("build${Util.toUpperFirstChar(buildTypeName)}Apk", _360Task, jiaguOutput, buildTypeName)
                        createPublishApkTask("build${Util.toUpperFirstChar(buildTypeName)}ApkOnly", null, jiaguOutput, buildTypeName)

                        // 单独为release生成两个渠道包任务
                        if ("release".equalsIgnoreCase(buildTypeName)) {
                            createChannelTask("buildReleaseChannelApk", _360Task, flavorName, versionCode, versionName, buildTypeName)
                            createChannelTask("buildReleaseChannelApkOnly", null, flavorName, versionCode, versionName, buildTypeName)
                        }
                    }
                }
            }
        }
    }

    private Task createChannelTask(taskName, jiaguTask, flavorName, versionCode, versionName, buildTypeName) {
        ChannelTask task = project.tasks.create(taskName, ChannelTask)
        task.flavorName = flavorName
        task.versionCode = versionCode
        task.versionName = versionName
        task.buildTypeName = buildTypeName
        task.setGroup("reinforce")

        if (jiaguTask != null)
            task.dependsOn(jiaguTask)
        println "创建${taskName}完毕"
        return task
    }

    private Task create360Task(signConfig, assembleTask, apkPath, jiaguOutput) {
        def subName = assembleTask.name.replace("assemble", "")

        // 创建加固task
        def taskName = "_360Jiagu${subName}"
        ReinforceTask360 task = project.tasks.create(taskName, ReinforceTask360)
        task.signingConfig = signConfig
        task.account = m360Extension.account
        task.password = m360Extension.password
        task.apkPath = apkPath
        task.outputApk = jiaguOutput

        task.setGroup("reinforce")
        task.dependsOn(assembleTask)

        // 创建加固不依赖assembleTask的加固任务
        def taskName2 = "${taskName}Only"
        ReinforceTask360 task2 = project.tasks.create(taskName2, ReinforceTask360)
        task2.signingConfig = signConfig
        task2.account = m360Extension.account
        task2.password = m360Extension.password
        task2.apkPath = apkPath
        task2.outputApk = jiaguOutput
        task2.setGroup("reinforce")

        println "创建${taskName}完毕"

        return task
    }

    private Task createPublishApkTask(taskName, jiaguTask, apkPath, buildTypeName) {
        PublishApkTask task = project.tasks.create(taskName, PublishApkTask)
        task.apkPath = apkPath
        task.buildTypeName = buildTypeName

        task.password = mPgyExtension.password
        task.deadTime = mPgyExtension.deadTime
        task.apiKey = mPgyExtension.apiKey
        task.dingTalkUrl = mPgyExtension.dingTalkUrl
        task.dingTalkSec = mPgyExtension.dingTalkSec

        task.setGroup("reinforce")
        if (jiaguTask != null)
            task.dependsOn(jiaguTask)

        println "创建${taskName}完毕"

        return task
    }
}