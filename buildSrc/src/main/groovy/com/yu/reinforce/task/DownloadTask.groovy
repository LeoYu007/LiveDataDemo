package com.yu.reinforce.task


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 发布到蒲公英
 */
class DownloadTask extends DefaultTask {
    private static final String url_mac = 'https://down.360safe.com/360Jiagu/360jiagubao_mac.zip'
    private static final String url_linux = 'https://down.360safe.com/360Jiagu/360jiagubao_linux_64.zip'
    private static final String url_windows = 'https://down.360safe.com/360Jiagu/360jiagubao_windows_32.zip'
    private static final String url_vasdolly = 'https://github.com/Tencent/VasDolly/raw/master/command/jar/VasDolly.jar'

    @TaskAction
    void download() {

    }

    def download360Jiagu() {

    }

    def downloadVasdollyJar() {

    }


    private String getApkSignerJarPath() {
        String jarPath = null
        def buildToolsVersion = project.android.buildToolsVersion

        // 获取local.properties配置的sdk目录
        String sdkDir = getSdkDirFromLocal()

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

    private String getSdkDirFromPath() {
        return System.getenv("ANDROID_HOME")
    }

    private String getSdkDirFromLocal() {
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

    private String buildApkSignerPath(String sdkDir, String buildToolsVersion) {
        String jarPath = sdkDir + File.separator + "build-tools" + File.separator + buildToolsVersion + File.separator + "lib" + File.separator + "apksigner.jar"
        println "apkSignerPath = $jarPath"
        return jarPath
    }

}