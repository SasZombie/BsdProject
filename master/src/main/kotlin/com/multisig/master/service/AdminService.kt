package com.multisig.master.service

import org.springframework.stereotype.Service
import java.io.File

@Service
class AdminService {
    private val logFile = File("logs/app.log")
    private var lastLine = countLines()


    private fun countLines(): Int {
        return logFile.useLines { lines -> lines.count() }
    }

    fun getLastLine(): String {
        return logFile.useLines { lines ->
            lines.lastOrNull { it.isNotBlank() } ?: "Empty logs"
        }
    }

    fun getNewLogs(): List<String> {
        val list = logFile.useLines { lines ->
            lines.drop(lastLine).toList()
        }

        //NU VREAU EU SA FIE +=
        //CINE ESTI TU SA IMI SCHIMBI
        //MIE CODUL
        @Suppress("ReplaceWithOperatorAssignment")
        lastLine = lastLine + list.size
        
        return list
    }
}