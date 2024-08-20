package tw.xserver.plugin.api.google.sheet

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import tw.xserver.plugin.api.google.sheet.serializer.AuthConfigSerializer
import java.io.File

class SheetsService(private val config: AuthConfigSerializer, private val folderPath: String) {
    private val httpTransport by lazy { GoogleNetHttpTransport.newTrustedTransport() }
    private val jsonFactory: JsonFactory by lazy { GsonFactory.getDefaultInstance() }
    private val credential by lazy { credential(httpTransport) }
    private val scopes by lazy { listOf(SheetsScopes.SPREADSHEETS) }

    val sheets: Sheets by lazy {
        Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("SheetsDSL")
            .build()
    }

    private fun credential(httpTransport: NetHttpTransport): Credential {
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, config.client_id, config.client_secret, scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(File(folderPath, "tokens")))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder()
            .setPort(config.port).build()

        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}