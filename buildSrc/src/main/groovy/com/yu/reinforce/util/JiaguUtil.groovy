package com.yu.reinforce.util

import org.gradle.api.Project

/**
 * 360加固
 */
class JiaguUtil {

    private static final String url_mac = 'https://down.360safe.com/360Jiagu/360jiagubao_mac.zip'
    private static final String url_linux = 'https://down.360safe.com/360Jiagu/360jiagubao_linux_64.zip'
    private static final String url_windows = 'https://down.360safe.com/360Jiagu/360jiagubao_windows_32.zip'


    /**
     * 360加固
     * @param project
     * @param apkPath
     * @param outputDir
     * @param userName
     * @param password
     * @return
     */
    static String reinforce(Project project, String apkPath, String outputDir, String userName, String password) {
        println '开始执行360加固'
        println "apkPath = $apkPath"
        println "outputDir = $outputDir"

        String jarPath = download360Jiagu(project)

        def loginCommand = "java -jar ${jarPath} -login ${userName} ${password}"
        def jiaguCommand = "java -jar ${jarPath} -jiagu ${apkPath} ${outputDir}"
        def retryCount = 0

        while (retryCount < 3) {
            project.exec {
                executable 'bash'
                args "-c", loginCommand
            }

            def result = project.exec {
                executable 'bash'
                args "-c", jiaguCommand
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

    private static String download360Jiagu(Project project) {
        String parentDir = project.parent.rootDir.parentFile.absolutePath
        String jarPath = parentDir + File.separator + "360jiagubao" + File.separator + "jiagu" + File.separator + "jiagu.jar"
        File zipFile = new File(parentDir + File.separator + "360jiagubao.zip")
        if (new File(jarPath).exists()) {
            println "jiagu.jar已存在，path = $jarPath"
        } else if (zipFile.exists()) {
            println "已存在360加固zip包，准备解压，path = ${zipFile.absolutePath}"
            UnzipUtil.unzipFileByKeyword(zipFile, new File(zipFile.parentFile, "360jiagubao"), null)
        } else {
            println "加固工具包不存在，准备下载工具包"
            String url = null
            String os = System.properties["os.name"].toString().toLowerCase()
            if (os.contains("mac")) {
                url = url_mac
            } else if (os.contains("windows")) {
                url = url_windows
            } else if (os.contains("linux")) {
                url = url_linux
            } else {
                throw RuntimeException("未知的操作系统，不能下载360加固保")
            }
            Util.downloadFile(url, zipFile.absolutePath)

            println "下载工具包成功，开始解压"
            UnzipUtil.unzipFileByKeyword(zipFile, new File(zipFile.parentFile, "360jiagubao"), null)
            println "解压工具包成功"
        }
        return jarPath
    }

}