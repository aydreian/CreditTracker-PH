## 2024-05-24 - [CRITICAL] Sensitive Data Leak in Logcat
**Vulnerability:** HttpLoggingInterceptor was configured with `Level.BODY` globally, which meant that in production builds, all network requests were logged to Logcat. This exposed sensitive Groq API keys via the `Authorization` header, as well as the content of the users' SMS messages and financial queries being sent to the AI API.
**Learning:** Hardcoding `Level.BODY` in logging interceptors is a common source of sensitive data leakage on Android, since Logcat can be read by other tools/apps under certain conditions or dumped during crash reporting.
**Prevention:** Always wrap logging levels in a debug build check (e.g. `if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE`) to ensure no sensitive data is logged in production.

## 2024-05-28 - [HIGH] Sensitive SMS and PII Leak in Logcat
**Vulnerability:** In `SmsReceiver.kt` and `SmsParserUseCase.kt`, sensitive user data including the raw SMS body and the parsed financial transaction details were being logged using `Log.d` without any debug-mode checks. This exposed sensitive financial PII to anyone with physical access to the device who could read the system logs or view crash reports.
**Learning:** Developers often leave debug logging enabled that captures sensitive information during development. In production, this becomes a critical privacy violation, especially for financial applications.
**Prevention:** Always wrap logging statements containing user input, sensitive data, or financial information within a `if (BuildConfig.DEBUG)` check to ensure they are excluded from production builds.
