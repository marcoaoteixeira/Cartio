# Stack

```kotlin
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")

implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:...")
implementation("androidx.security:security-crypto:...")
```

Using:

- Credential Manager
- Google ID Token Sign-In
- OAuth token exchange
- Raw Drive REST endpoints
- OkHttp transport

---

# 1. Auth Layer (Modern)

## Auth Contract

```kotlin
interface GoogleAuthManager {

    suspend fun signIn(): GooglePrincipal

    suspend fun getAccessToken(): String

    suspend fun signOut()
}

data class GooglePrincipal(
    val email: String,
    val sub: String
)
```

---

## Credential Manager Sign-In

```kotlin
class GoogleAuthManagerImpl(
    private val context: Context,
    private val credentialManager: CredentialManager
) : GoogleAuthManager {

    override suspend fun signIn(): GooglePrincipal {

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential

        val googleCredential =
            GoogleIdTokenCredential.createFrom(
                credential.data
            )

        return GooglePrincipal(
            email = googleCredential.id,
            sub = googleCredential.id
        )
    }
```

---

## Access Token Acquisition

Use Google Identity OAuth scope consent.

```kotlin
override suspend fun getAccessToken(): String {

    // Draft placeholder.
    // Production uses GIS authorization APIs.

    return fetchOAuthTokenForScope(
       "https://www.googleapis.com/auth/drive.appdata"
    )
}
```

Important:

Use:

```text
AuthorizationClient.authorize()
```

for incremental consent.

---

# 2. Drive REST Client (No Google Java SDK)

```kotlin
class DriveRestClient(
    private val auth: GoogleAuthManager,
    private val http: OkHttpClient
)
```

Helper:

```kotlin
private suspend fun authorizedRequest(
    builder: Request.Builder
): Request {

    val token = auth.getAccessToken()

    return builder
      .header(
         "Authorization",
         "Bearer $token"
      )
      .build()
}
```

---

# 3. Upload Backup Using Multipart REST

Endpoint:

```text
POST https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart
```

```kotlin
suspend fun uploadBackup(file: File) {

    val metadata =
        """
        {
          "name":"backup_${System.currentTimeMillis()}.db.enc",
          "parents":["appDataFolder"]
        }
        """.trimIndent()

    val multipartBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "metadata",
            null,
            metadata.toRequestBody(
                "application/json".toMediaType()
            )
        )
        .addFormDataPart(
            "file",
            file.name,
            file.asRequestBody(
                "application/octet-stream".toMediaType()
            )
        )
        .build()

    val request = authorizedRequest(
        Request.Builder()
           .url(
             "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
           )
           .post(multipartBody)
    )

    http.newCall(request)
        .execute()
        .use {
            check(it.isSuccessful)
        }
}
```

---

# 4. List Backups

```kotlin
suspend fun listBackups(): List<DriveBackup> {

    val request = authorizedRequest(
       Request.Builder()
          .url(
           "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&orderBy=createdTime desc"
          )
    )

    val response =
        http.newCall(request)
            .execute()
            .body!!.string()

    return json.decodeFromString(response)
}
```

---

# 5. Delete Old Backups (Retention = 3)

```kotlin
suspend fun pruneOldBackups() {

   listBackups()
      .drop(3)
      .forEach {

          val req = authorizedRequest(
             Request.Builder()
                .delete()
                .url(
                 "https://www.googleapis.com/drive/v3/files/${it.id}"
                )
          )

          http.newCall(req).execute()
      }
}
```

Same rule remains:

Upload first.
Prune second.

---

# 6. Download Restore File

```kotlin
suspend fun downloadBackup(
   fileId: String
): File {

    val request = authorizedRequest(
      Request.Builder()
        .url(
          "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        )
    )

    val out = File(cacheDir,"restore.enc")

    http.newCall(request)
       .execute()
       .use { response ->
          out.outputStream().use {
              response.body!!.byteStream()
                   .copyTo(it)
          }
       }

    return out
}
```

---

# 7. Safer Snapshot Upgrade — Use VACUUM INTO

Replace raw copy approach.

Much better:

```kotlin
suspend fun createSnapshot(): File {

   val backup = File(
      context.cacheDir,
      "snapshot.db"
   )

   db.openHelper
      .writableDatabase
      .execSQL(
        "VACUUM INTO '${backup.absolutePath}'"
      )

   return backup
}
```

This avoids WAL consistency pitfalls.

Strongly preferred.

---

# 8. Better Encryption (Jetpack Security + Keystore)

```kotlin
class KeystoreCryptoManager {

   private val keyAlias = "db_backup_key"

   fun encrypt(file: File): File {
      // AES-GCM with key in Android Keystore.
   }
}
```

Use:

- AES-GCM
- Random IV
- Authenticated encryption

Avoid custom crypto.

---

# 9. Modern Background Scheduling

Use expedited manual backup:

```kotlin
OneTimeWorkRequestBuilder<BackupWorker>()
   .setExpedited(
      OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
   )
```

Periodic backup remains:

```kotlin
PeriodicWorkRequestBuilder<BackupWorker>(1, DAYS)
```

---

# 10. Token Expiration Handling

Important with raw REST.

Interceptor:

```kotlin
class AuthRefreshingInterceptor(
   private val auth: GoogleAuthManager
): Interceptor {

 override fun intercept(
   chain: Interceptor.Chain
 ): Response {

   var req = chain.request()

   var response = chain.proceed(req)

   if(response.code == 401) {
      val newToken = runBlocking {
          auth.getAccessToken()
      }

      req = req.newBuilder()
         .header(
           "Authorization",
           "Bearer $newToken"
         )
         .build()

      response.close()
      return chain.proceed(req)
   }

   return response
 }
}
```

Very useful for reliability.
