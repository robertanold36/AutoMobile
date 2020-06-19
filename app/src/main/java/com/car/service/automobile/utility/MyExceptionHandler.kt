package com.car.service.automobile.utility

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess


class MyExceptionHandler(context: Context, c: Class<*>) :
    Thread.UncaughtExceptionHandler {
    private val myContext: Context = context
    private val myActivityClass: Class<*> = c
    override fun uncaughtException(
        thread: Thread,
        exception: Throwable
    ) {
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))
        Log.d("MainActivity","$stackTrace") // You can use LogCat too
        val intent = Intent(myContext, myActivityClass)
        val s: String = stackTrace.toString()
        //you can use this String to know what caused the exception and in which Activity
        intent.putExtra(
            "uncaughtException",
            "Exception is: $stackTrace"
        )
        intent.putExtra("stacktrace", s)
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            myContext.startActivity(intent)
            //for restarting the Activity
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }

    }

}