#!/bin/sh

#腾讯多渠道打包命令
jarPath=$1
channelPath=$2 #渠道配置
baseApkPath=$3 #原apk地址
outputDir=$4 #输出目录

java -jar ${jarPath} put -c ${channelPath} ${baseApkPath} ${outputDir}