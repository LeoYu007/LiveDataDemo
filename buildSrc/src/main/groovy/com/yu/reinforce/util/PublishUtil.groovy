package com.yu.reinforce.util

import com.yu.reinforce.entity.PublishMessage
import org.json.JSONObject

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat


static PublishMessage uploadToPgy(String apiKey, String apkPath, String password, int deadTime, String buildTypeName) {
    // 蒲公英上传
    String url = "https://www.pgyer.com/apiv2/app/upload"
    Map<String, String> params = new HashMap<>()
    params.put("_api_key", apiKey)
    if (Util.isEmpty(password)) {
        params.put("buildInstallType", "1")
    } else {
        params.put("buildInstallType", "2")
        params.put("buildPassword", password)
    }
    if (deadTime > 0) {
        params.put("buildInstallDate", "2")
    } else {
        Date today = new Date()
        Calendar calendar = today.toCalendar()
        calendar.add(Calendar.DATE, deadTime)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")

        params.put("buildInstallDate", "1")
        params.put("buildInstallStartDate", sdf.format(today))
        params.put("buildInstallEndDate", sdf.format(calendar.time))
    }

    String body = Util.uploadFile(url, apkPath, null, params)

    if (body != null) {
        // 发送钉钉消息
        def data = new JSONObject(body).optJSONObject("data")
        def shortCutUrl = data.optString("buildShortcutUrl")
        def appQRCodeURL = data.optString("buildQRCodeURL")
        def appName = data.optString("buildName")
        def appVersion = data.optString("buildVersion")
        def appUpdated = data.optString("buildUpdated")
        def pgyBuildVersion = data.optString("buildBuildVersion")
        def appUrl = "https://www.pgyer.com/${shortCutUrl}"
        def branchName = getGitBranch()

        return new PublishMessage(appName, appVersion, appUrl, appQRCodeURL, branchName, buildTypeName, password, appUpdated, pgyBuildVersion)
    }
    return null
}

static def sendMessageToDingDing(project, String dingTalkUrl, String dingTalkSec, PublishMessage message) {
    if (Util.isNotEmpty(dingTalkUrl) && Util.isNotEmpty(dingTalkSec)) {
        String dingDingUrl = buildDingTalkUrl(dingTalkUrl, dingTalkSec)

        String pwdLine = ""
        if (message.password != null && message.password.length() != 0) {
            pwdLine = "- 密码：${message.password}"
        }

        def title = "Android-${message.appName}-${message.appVersion}-${message.buildType} (build ${message.pgyBuildVersion})版本更新"
        def text = """
### ${title}
- 分支：${message.branchName}
- 更新时间：${message.date}
- [点击下载应用](${message.appUrl})
${pwdLine}
"""
        JSONObject data = new JSONObject()
        data.put("title", title)
        data.put("text", text)

        JSONObject obj = new JSONObject()
        obj.put("markdown", data)
        obj.put("msgtype", "markdown")

        def command = "curl -H 'Content-Type: application/json' -d '${obj.toString()}' '${dingDingUrl}'"
        println "command = $command"
        project.exec {
            executable 'bash'
            args '-c', command
        }
    } else {
        print "发送钉钉消息失败，url或者secret为空"
    }
}

/**
 * 钉钉和飞书的群机器人，发送消息需要签名校验，这里生成对应的签名
 * 算法其实都是一样的：timestamp + “\n” + 密钥 当做签名字符串，使用 HmacSHA256 算法计算签名，再进行 Base64 编码
 *
 * @param secret 钉钉或者飞书的秘钥
 * @return
 */
private static String buildSignature(Long timestamp, String secret) {

    String stringToSign = timestamp + "\n" + secret
    Mac mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"))
    byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"))
    String sign = new String(Base64.encoder.encode(signData))

    return sign
}

/**
 * 生成钉钉机器人url，签名校验信息以url传参的方式添加（飞书是post参数）
 * @param secret
 * @param baseUrl 钉钉机器人的url，包含access_token
 * @return
 */
private static String buildDingTalkUrl(String baseUrl, String secret) {
    if (secret == null || secret == "") return baseUrl
    Long timestamp = System.currentTimeMillis()
    String signature = buildSignature(timestamp, secret)
    String encodeString = URLEncoder.encode(signature)
    return "${baseUrl}&timestamp=${timestamp}&sign=${encodeString}"
}


private static String getGitBranch() {
    //判断是否处于Jenkins编译环境
    if (isInJenkins()) {
        Map<String, String> env = System.getenv()
        return env["BRANCH"]
    } else {
        return 'git symbolic-ref --short -q HEAD'.execute().text.trim()
    }
}

private static boolean isInJenkins() {
    Map<String, String> map = System.getenv()
    if (map == null) {
        return false
    }
    String str = map.get("Path")
    if (str != null) {
        return false
    } else {
        str = ""
        Iterator it = map.iterator()
        while (it.hasNext()) {
            str += it.next()
        }
        return str.contains("jenkins")
    }
}