package one.svt.plugins.solanawallet
// package com.solanamobile.mobilewalletadapter.capacitor

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import com.solana.mobilewalletadapter.clientlib.protocol.JsonRpc20Client
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationIntentCreator
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationScenario
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


@CapacitorPlugin(name = "SolanaMobileWalletAdapter")
class SolanaMobileWalletAdapterModule: Plugin(), CoroutineScope {

    data class SessionState(
        val client: MobileWalletAdapterClient,
        val localAssociation: LocalAssociationScenario,
    )

    override val coroutineContext =
        Dispatchers.IO + CoroutineName("SolanaMobileWalletAdapterModuleScope") + SupervisorJob()

    companion object {
        private const val ASSOCIATION_TIMEOUT_MS = 10000
        private const val CLIENT_TIMEOUT_MS = 90000

        // Used to ensure that you can't start more than one session at a time.
        private val mutex: Mutex = Mutex()
        private var sessionState: SessionState? = null
    }

    private fun getName(): String {
        return "SolanaMobileWalletAdapter"
    }

    @ActivityCallback
    private fun handleLocalAssociation(call: PluginCall?, result: ActivityResult) {
        if (call == null) {
            return
        }
        if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(getName(), "Local association cancelled by user, ending session")
            call.reject(
                "Session not established: Local association cancelled by user",
                LocalAssociationScenario.ConnectionFailedException("Local association cancelled by user")
            )
//            // TODO: https://github.com/solana-mobile/mobile-wallet-adapter/blob/db64eb559547ebd5abc4fe7e4e94865e694b84ff/js/packages/mobile-wallet-adapter-protocol/android/src/main/java/com/solanamobile/mobilewalletadapter/reactnative/SolanaMobileWalletAdapterModule.kt#L86C34-L86C34
//            localAssociation.close()
        }
    }
    
    @PluginMethod
    fun echo(call: PluginCall) {
        val value = call.getString("value")
        value?.let { Log.d("TAG echo: ", it) }
    }

    @PluginMethod
    fun startSession(call: PluginCall) = launch {
        mutex.lock()

        Log.d(getName(), "startSession with data ${call.data}")

        try {
            val uriPrefix = call.getString("baseUri")?.let { Uri.parse(it) }
            val localAssociation = LocalAssociationScenario(
                CLIENT_TIMEOUT_MS,
            )
            val intent = LocalAssociationIntentCreator.createAssociationIntent(
                uriPrefix,
                localAssociation.port,
                localAssociation.session
            )
            startActivityForResult(call, intent, "handleLocalAssociation")
            val client = withContext(Dispatchers.IO) {
                localAssociation.start().get(ASSOCIATION_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            }
            sessionState = SessionState(client, localAssociation)
            call.resolve()
        } catch (e: ActivityNotFoundException) {
            Log.e(getName(), "Found no installed wallet that supports the mobile wallet protocol", e)
            cleanup()
            call.reject("ERROR_WALLET_NOT_FOUND", e)
        } catch (e: TimeoutException) {
            Log.e(getName(), "Timed out waiting for local association to be ready", e)
            cleanup()
            call.reject("Timed out waiting for local association to be ready", e)
        } catch (e: InterruptedException) {
            Log.w(getName(), "Interrupted while waiting for local association to be ready", e)
            cleanup()
            call.reject("Interrupted while waiting for local association to be ready", e)
        } catch (e: ExecutionException) {
            Log.e(getName(), "Failed establishing local association with wallet", e.cause)
            cleanup()
            call.reject("Failed establishing local association with wallet", e)
        } catch (e: Throwable) {
            Log.e(getName(), "Failed to start session", e)
            cleanup()
            call.reject("Failed to start session", e.message)
        }
    }

    @PluginMethod
    fun invoke(call: PluginCall) = sessionState?.let {
        val method = call.getString("method", "unknown")!!
        val params = call.getObject("params")
        try {
            Log.d(getName(), "invoke `$method` with params $params")
            val result = it.client.methodCall(method, params, CLIENT_TIMEOUT_MS).get() as JSObject
            call.resolve(result)
        } catch (e: ExecutionException) {
            val cause = e.cause
            if (cause is JsonRpc20Client.JsonRpc20RemoteException) {
                val userInfo = JSObject()
                userInfo.put("jsonRpcErrorCode", cause.code)
                call.reject("JSON_RPC_ERROR", cause, userInfo)
            } else {
                throw e
            }
        } catch (e: Throwable) {
            Log.e(getName(), "Failed to invoke `$method` with params $params", e)
            call.reject("Failed to invoke `$method` with params $params", e.message)
        }
    } ?: throw NullPointerException("Tried to invoke `${call.getString("method")}` without an active session")

    @PluginMethod
    fun endSession(call: PluginCall) = sessionState?.let {
        launch {
            Log.d(getName(), "endSession")
            try {
                withContext(Dispatchers.IO) {
                    it.localAssociation.close()
                            .get(ASSOCIATION_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                }
                cleanup()
                call.resolve()
            } catch (e: TimeoutException) {
                Log.e(getName(), "Timed out waiting for local association to close", e)
                cleanup()
                call.reject("Failed to end session", e)
            } catch (e: Throwable) {
                Log.e(getName(), "Failed to end session", e)
                cleanup()
                call.reject("Failed to end session", e.message)
            }
        }
    } ?: throw NullPointerException("Tried to end a session without an active session")

    private fun cleanup() {
        sessionState = null
        if (mutex.isLocked) {
            mutex.unlock()
        }
    }
}
