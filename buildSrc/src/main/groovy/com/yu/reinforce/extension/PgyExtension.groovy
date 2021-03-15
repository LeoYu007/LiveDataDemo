package com.yu.reinforce.extension

class PgyExtension {

    // 蒲公英的key
    String apiKey
    // 密码为空则公开发布
    String password
    // 有效期，单位天，如果小于等于0则长期有效
    int deadTime

    // 钉钉机器人配置
    String dingTalkUrl
    String dingTalkSec
}