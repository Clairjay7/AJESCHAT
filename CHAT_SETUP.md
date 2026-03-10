# AJESCHAT – Login + Chat (AJES Backend)

## What’s included

- **Login:** POST `auth/login` with username/password; session via cookies (same as web).
- **Chat:** Same behavior as web: user list, conversation, send, unsend (for me / for everyone), censorship, polling.
- **UI:** Login screen → Chat list → Conversation (Compose, Material3, green theme).

## Base URL

- **Emulator:** `http://10.0.2.2/AJES/` (already set in debug `BuildConfig`).
- **Real device (same network as PC):** Use your PC’s IP, e.g. `http://192.168.1.100/AJES/`.
  - Change in `app/build.gradle.kts`: `buildConfigField("String", "BASE_URL", "\"http://YOUR_IP/AJES/\"")` for the build type you use.
- **Production:** Set in `release` build type to your HTTPS URL.

All API paths are relative to this base (e.g. `auth/login`, `api/chat/users`, `chat/messages`, `chat/send`, `chat/unsend`).

## Backend (AJES)

1. **Cookies:** The app uses one OkHttp client with a persistent `CookieJar`. After login, the same session cookie is sent on every chat request.
2. **CSRF:** The app GETs a page (e.g. `/chat`), parses the CSRF token from HTML (`csrf_test_name` or meta tag), and sends it with POSTs.
3. **User list:** The app calls **GET `api/chat/users`**. Add this endpoint in AJES (see `backend-addons/README.md` and `Api_Chat_users_example.php`) and return `{ "users": [ { "id", "name", "role", "has_chat" } ] }` using the same logic as your web `getChatUserList()`.
4. **Optional:** JSON response for `POST auth/login` when `Accept: application/json` (see `backend-addons/Auth_login_JSON_example.php`).

Existing chat endpoints (`GET chat/messages?with=`, `POST chat/send`, `POST chat/unsend`) are used as-is; no backend change required if the web already uses them.

## Running

1. Open the project in Android Studio.
2. Set BASE_URL in `app/build.gradle.kts` if needed (see above).
3. Ensure AJES is running (e.g. XAMPP) and the `api/chat/users` endpoint is added.
4. Run the app on emulator or device and log in with the same credentials as the web.
