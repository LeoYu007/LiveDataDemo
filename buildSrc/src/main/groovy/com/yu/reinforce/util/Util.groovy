package com.yu.reinforce.util


import com.android.builder.model.SigningConfig
import okhttp3.*

import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * 用脚本签名apk
 * @param apkPath
 * @param shellPath
 * @return
 */
static String signApk(String apkPath, String shellPath, SigningConfig signingConfig, String jarPath, String outputPath = "") {
    println "开始签名 apkPath = ${apkPath}"

    def keystore = signingConfig.storeFile.absolutePath
    def storePass = signingConfig.storePassword
    def alias = signingConfig.keyAlias
    def keypass = signingConfig.keyPassword

    def output = outputPath
    if (isEmpty(output)) {
        output = apkPath.substring(0, apkPath.lastIndexOf('.'))
                .replace("unsigned", "") + "_signed.apk"
    }

    doCommand('sh ' + shellPath + ' ' + keystore + ' ' + alias + ' ' + storePass + ' ' + keypass + ' ' + output + ' ' + apkPath + ' ' + jarPath)
    println "签名完成 output = ${output}"
    return output
}

// 执行360加固
static def reinforceBy360(String apkPath, String shellPath, String userName, String password, String output, String jarPath) {
    println "开始加固 apkPath = ${apkPath}"
    doCommand('sh ' + shellPath + ' ' + userName + ' ' + password + ' ' + apkPath + ' ' + output + ' ' + jarPath)
    println "加固完成 output = ${output}"
}

static def doCommand(command) {
    println('run command:' + command)
    def process = Runtime.getRuntime().exec(command)
    def read = new BufferedReader(new InputStreamReader(process.inputStream))
    def line = read.readLine()
    while (line != null) {
        println(line)
        line = read.readLine()
    }
    read.close()
    def error = new BufferedReader(new InputStreamReader(process.errorStream))
    line = error.readLine()
    def isError = false
    while (line != null) {
        println(line)
        line = error.readLine()
        isError = true
    }

    process.destroy()
    // 如果出现错误直接抛出异常 终止执行
    if (isError) throw new Exception("cmd命令执行失败，$this")
}

/**
 * 下载文件
 * @param url
 * @param filePath
 * @return
 */
static def downloadFile(url, filePath) {
    println('downloadFile run\n')
    println('url->' + url)
    println('filePath->' + filePath)
    def clientBuilder = new OkHttpClient.Builder()
    clientBuilder.connectTimeout(10, TimeUnit.SECONDS)
    clientBuilder.readTimeout(60, TimeUnit.SECONDS)
    OkHttpClient client = clientBuilder.build()
    def request = new Request.Builder()
            .url(url)
            .get()
            .build()
    def response = client.newCall(request).execute()
    def write = new BufferedOutputStream(new FileOutputStream(filePath, false))
    def read = new BufferedInputStream(response.body().byteStream())
    def bytes = new byte[1024]
    def bytesRead = 0
    while ((bytesRead = read.read(bytes)) != -1) {
        write.write(bytes, 0, bytesRead)
    }
    read.close()
    write.flush()
    write.close()
}

static String uploadFile(String url, String filePath, Map<String, String> headers, Map<String, String> params) {

    Headers.Builder headerBuilder = new Headers.Builder()
    if (headers != null && headers.size() > 0) {
        headers.forEach { key, value ->
            headerBuilder.add(key, value)
            println "header:  key = ${key}  value = ${value}"
        }
    }

    File file = new File(filePath)

    MultipartBody.Builder builder = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(file, MediaType.parse("multipart/form-data")))

    if (params != null && params.size() > 0) {
        params.forEach { key, value ->
            builder.addFormDataPart(key, value)
            println "param:  key = ${key}  value = ${value}"
        }
    }

    def request = new Request.Builder()
            .url(url)
            .headers(headerBuilder.build())
            .post(builder.build())
            .build()

    def clientBuilder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
    OkHttpClient client = clientBuilder.build()

    println "开始上传文件 file = ${filePath}"

    def retryCount = 0
    String bodyStr = null
    while (retryCount < 3) {
        try {
            Response response = client.newCall(request.newBuilder().build()).execute()
            if (response.isSuccessful()) {
                bodyStr = response.body().string()
                println "response body = ${bodyStr}"
                return bodyStr
            } else {
                println "上传失败 code = ${response.code()}  message = ${response.message()}"
                println "上传发生异常，正在第 ${retryCount + 1} 次重试"
            }
        } catch (Exception e) {
            println "上传失败: ${e.toString()}"
            println "上传发生异常，正在第 ${retryCount + 1} 次重试"
        }
        retryCount++

        try {
            Thread.sleep(1000)
        } catch (Exception e) {
        }

    }

    if (bodyStr == null) {
        println "上传失败，请重新构建或者手动上传，文件地址：${filePath}"
    }

    return bodyStr
}

/**
 * 获取文件md5
 * @param filePath 文件路径
 * @return
 */
static def getFileMd5(filePath) {
    def FILE_READ_BUFFER_SIZE = 1024
    MessageDigest digester = MessageDigest.getInstance("MD5")
    def stream = new FileInputStream(filePath)
    int bytesRead
    byte[] buf = new byte[FILE_READ_BUFFER_SIZE]
    while ((bytesRead = stream.read(buf)) >= 0) {
        digester.update(buf, 0, bytesRead)
    }
    def md5code = new BigInteger(1, digester.digest()).toString(16)// 16进制数字
    // 如果生成数字未满32位，需要前面补0
    for (int i = 0; i < 32 - md5code.length(); i++) {
        md5code = "0" + md5code
    }
    return md5code
}

static String toUpperFirstChar(String content) {
    if (isEmpty(content)) {
        return ""
    }
    char[] chars = content.toCharArray()
    chars[0] -= 32
    return String.valueOf(chars)
}

static def isNotEmpty(String str) {
    return str != null && !str.isEmpty()
}

static def isEmpty(String str) {
    return str == null || str.isEmpty()
}