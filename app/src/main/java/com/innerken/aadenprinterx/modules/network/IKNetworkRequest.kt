package com.innerken.aadenprinterx.modules.network

import com.innerken.aadenprinterx.dataLayer.IKResponseModel

typealias IKResponse<T> = IKResponseModel<T>


data class IKCloudResponseModel<T>(val code: Int, val data: T, val message: String?)


object SafeRequest {
    @JvmName("handleIK")
    suspend fun <T> handle(shouldThrow: Boolean = false, action: suspend () -> IKResponse<T>): T? {
        return try {
            val res = action()
            if (res.status == "good") {
                return res.content
            } else {
                throw Exception(res.info)
            }
        } catch (e: Exception) {
            if (shouldThrow) {
                throw e
            } else {
                e.printStackTrace()
            }
            null
        }
    }


}

object SafePlainRequest{
    suspend fun <T> handle(shouldThrow: Boolean = false, action: suspend () -> T): T? {
        return try {
            val res = action()
            return res
        } catch (e: Exception) {
            if (shouldThrow) {
                throw e
            } else {
                e.printStackTrace()
            }
            null
        }
    }
}