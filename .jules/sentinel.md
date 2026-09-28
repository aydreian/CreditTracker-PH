## 2024-05-24 - [CRITICAL] Sensitive Data Leak in Logcat
**Vulnerability:** HttpLoggingInterceptor was configured with `Level.BODY` globally, which meant that in production builds, all network requests were logged to Logcat. This exposed sensitive Groq API keys via the `Authorization` header, as well as the content of the users' SMS messages and financial queries being sent to the AI API.
**Learning:** Hardcoding `Level.BODY` in logging interceptors is a common source of sensitive data leakage on Android, since Logcat can be read by other tools/apps under certain conditions or dumped during crash reporting.
**Prevention:** Always wrap logging levels in a debug build check (e.g. `if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE`) to ensure no sensitive data is logged in production.

## 2024-05-24 - [CRITICAL] Sensitive Data Leak in Logcat
**Vulnerability:** Parsed SMS data, including merchants, amounts, and partial card numbers, was being directly logged to Logcat upon successful parsing in `SmsParserUseCase`.
**Learning:** Logging entire data objects without obfuscation or filtering can inadvertently expose Personally Identifiable Information (PII) or financial data, even if it's just 'debug' logging.
**Prevention:** Avoid logging complete data objects containing sensitive information. Log only necessary status messages or identifiers (e.g., 'Successfully parsed via local PH bank regex') rather than the full data payload.
