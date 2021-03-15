package com.yu.reinforce.entity


class PublishMessage {
    String title
    String message
    String url
    String imageUrl

    PublishMessage(String title, String message, String url, String imageUrl) {
        this.title = title
        this.message = message
        this.imageUrl = imageUrl
        this.url = url
    }
}