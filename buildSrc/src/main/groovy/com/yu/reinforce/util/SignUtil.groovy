package com.yu.reinforce.util

import com.android.builder.model.SigningConfig
import org.gradle.api.Project


/**
 * 签名apk
 * @param project
 * @param signingConfig
 * @param apk
 */
static String signApk(Project project, SigningConfig signingConfig, String apk) {
    println "开始签名，apk = $apk"
    def jarPath = getApkSignerJarPath(project)
    def output = apk.substring(0, apk.lastIndexOf('.')).replace("unsigned", "") + "_signed.apk"
    def keystore = signingConfig.storeFile.absolutePath
    def storePass = signingConfig.storePassword
    def alias = signingConfig.keyAlias
    def keypass = signingConfig.keyPassword

    // 执行签名
    String command = "java -jar ${jarPath} sign --ks ${keystore} --ks-key-alias ${alias} --ks-pass pass:${storePass} --key-pass pass:${keypass} --out ${output} ${apk}"
    println command
    project.exec {
        executable 'bash'
        args "-c", command
    }

    // 验证签名
    println "开始验证签名"
    command = "java -jar ${jarPath} verify -v ${output}"
    project.exec {
        executable 'bash'
        args "-c", command
    }

    println "删除未签名文件"
    // 删除未签名的apk
    project.delete {
        delete apk
    }

    println "签名完毕，outputPath = $output"
    return output
}

/**
 * 获取apksigner.jar地址
 * @param project
 * @return
 */
private static String getApkSignerJarPath(Project project) {
    String jarPath = null
    def buildToolsVersion = project.android.buildToolsVersion

    // 获取local.properties配置的sdk目录
    String sdkDir = getSdkDirFromLocal(project)

    // local.properties获取失败则尝试获取环境变量配置的ANDROID_HOME
    if (sdkDir == null) {
        sdkDir = getSdkDirFromPath()
    }

    if (sdkDir == null) {
        println "未找到android sdk目录，请在以下方式中配置任意一种："
        println "1.在项目根目录创建local.properties文件，配置sdk.dir=xxx，xxx为具体的sdk目录"
        println "2.配置ANDROID_HOME环境变量，例如：ANDROID_HOME=/Users/xxx/Library/Android/sdk"
    } else {
        jarPath = buildApkSignerPath(sdkDir, buildToolsVersion)
    }

    // 校验jar是否存在
    if (jarPath == null || !new File(jarPath).exists()) {
        println "没有找到apksigner.jar，请检查是否安装有Android sdk，是否有对应版本的buildTool(buildToolsVersion = $buildToolsVersion)"
    }

    return jarPath
}

private static String getSdkDirFromPath() {
    return System.getenv("ANDROID_HOME")
}

private static String getSdkDirFromLocal(Project project) {
    def local = new File(project.parent.rootDir.absolutePath + File.separator + "local.properties")
    if (local.exists()) {
        Properties p = new Properties()
        p.load(new FileInputStream(local))
        def sdkPath = p.getProperty("sdk.dir")
        if (sdkPath == null || "" == sdkPath) {
            println 'local.properties 中的 sdk.dir 不存在'
        } else {
            return sdkPath
        }
    } else {
        println 'local.properties 配置不存在'
    }

    return null
}

private static String buildApkSignerPath(String sdkDir, String buildToolsVersion) {
    String jarPath = sdkDir + File.separator + "build-tools" + File.separator + buildToolsVersion + File.separator + "lib" + File.separator + "apksigner.jar"
    println "apkSignerPath = $jarPath"
    return jarPath
}