package com.innerken.aadenprinterx.dataLayer.api

import com.innerken.aadenprinterx.dataLayer.model.PrintQuestEntity
import com.innerken.aadenprinterx.dataLayer.model.RestaurantInfoEntity
import com.innerken.aadenprinterx.modules.network.IKResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PrinterService {
    @GET("PrintRecord.php?op=getAllRecords&limit=30")
    suspend fun getPrintQuestEntities(): IKResponse<List<PrintQuestEntity>>

    @GET("Printer.php?op=getShangMiPrintQuest")
    suspend fun getShangMiPrintQuest(): IKResponse<List<PrintQuestEntity>>

    @FormUrlEncoded
    @POST("Printer.php?op=reportPrintStatus")
    suspend fun reportPrintStatus(
        @Field("id") id: Int,
        @Field("succeed") succeed: Int = 1,
        @Field("message") message: String = "ShangMi-NewVersion",
    ): IKResponse<Any>

    //我们自己集成在前端的重打接口
    @GET("Printer.php?op=reprintWithDefault")
    suspend fun reprintWithDefault(
        @Query("id") id: Int,
        @Query("_targetPrinter") targetPrinterId: String,
    ): IKResponse<String>

    @GET("Restaurant.php?op=view")
    suspend fun getRestaurantInfo(): IKResponse<List<RestaurantInfoEntity>>
}