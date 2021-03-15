#!/bin/sh

keystore=$1
alias=$2
storePass=$3
keypass=$4

output=$5 #输出地址
origin=$6 #原apk地址

jarPath=$7
#jarPath=../reinforce/signature/apksigner.jar


#默认同时执行v1+v2签名，
java -jar ${jarPath} sign --ks "${keystore}" --ks-key-alias "${alias}" --ks-pass pass:"${storePass}" --key-pass pass:"${keypass}" --out "${output}" "${origin}"

#验证签名
#java -jar ${jarPath} verify -v "${output}"

rm -f "$origin"