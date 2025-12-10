package com.innerken.aadenprinterx.dataLayer

data class IKResponseModel<T>(val status: String, val content: T, val info: String)