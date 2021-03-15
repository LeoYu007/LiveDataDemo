#!/bin/sh

userName=$1
password=$2

origin=$3 #原apk地址
output=$4 #输出地址

jarPath=$5
#jarPath=../reinforce/jiagu/jiagu.jar

#登录
java -jar ${jarPath} -login ${userName} ${password}
#加固
java -jar ${jarPath} -jiagu ${origin} ${output}