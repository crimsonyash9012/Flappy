package com.example.flappy.single

import android.content.Context
import com.example.flappy.utils.Constant
import io.appwrite.Client

object AppwriteClientSingleton {
    private var client: Client? = null

    fun getClient(context: Context): Client{
        synchronized(this){
            if(client ==null){
                client = Client(context)
                    .setEndpoint(Constant.END_POINT)
                    .setProject(Constant.PROJECT_ID)
            }
            return client!!
        }
    }
}