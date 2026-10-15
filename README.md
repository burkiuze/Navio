# Navio

Navio, iki modlu bir yapay zekâ asistanıdır (Android / Jetpack Compose):

- **Chat** — OpenRouter modelleriyle sohbet. Web araması OpenRouter'ın `web` eklentisiyle yapılır; yanıtların üstünde kaynak kartları gösterilir. Sohbette "her sabah 9'da … özetle" gibi bir istek yazarsanız yapay zekâ `create_automation` aracıyla otomasyonu kendisi kurar.
- **Computer** — Zamanlanmış otomasyonlar (saatlik / günlük / haftalık / elle). Her çalıştırmada talimat web aramasıyla yürütülür, sonuç kaydedilir ve bildirim olarak gelir (WorkManager). Otomasyonlar alttaki kutuya doğal dille yazılarak ya da `+` ile form üzerinden kurulabilir.

## Giriş (Clerk: Google, GitHub, GitLab)

Giriş Clerk ile yapılır (`Navio` uygulaması). Uygulama Clerk Frontend API'sini doğrudan kullanır (resmi Android SDK'sı bu projeden daha yeni Kotlin/Compose istiyor): giriş tarayıcıda açılır ve `clerk://<applicationId>.callback` adresine döner.

Geliştirme ortamında Google, GitHub ve GitLab, Clerk'in ortak OAuth bilgileriyle çalışır; ek ayar gerekmez. Production'a geçerken her sağlayıcı için kendi OAuth bilgilerinizi Clerk panelinden girin.

## Yapılandırma (Forge.env + Vercel)

Servis adresleri ve anahtarlar bu repoda tutulmaz:

- **Forge.env** (özel repo): `NAVIO_CLERK_FAPI`, `NAVIO_AI_PROXY`, `NAVIO_CONFIG_URL` ve isteğe bağlı `OPENROUTER_API_KEY`. Forge.env, Navio'nun yanına klonlanmışsa derleme bu değerleri `BuildConfig`'e alır.
- **Vercel `navio-config`**: aynı değerleri proje ortam değişkenlerinde tutar ve `/api/config` ile döndürür. Forge.env olmadan yapılan derlemeler (ör. GitHub Actions) değerleri açılışta buradan çeker ve önbelleğe alır. Repoda yalnızca bu adresin varsayılanı bulunur.

## Yapay zekâ anahtarı

Kullanıcı anahtar girmez. İstekler Vercel'deki **Forge AI proxy**'sine gider; OpenRouter anahtarı orada sunucu tarafında durur. Model listesi de proxy'nin `/models` ucundan gelir (ücretsiz modeller, varsayılan `openrouter/free`).

Yedek olarak, derleme sırasında Forge.env bulunursa anahtar `BuildConfig.OPENROUTER_KEY`'e gömülür ve proxy'ye ulaşılamazsa doğrudan OpenRouter kullanılır. Arama sırası: `OPENROUTER_API_KEY` ortam değişkeni → `Navio/.env` → `../Forge.env/.env` → `Navio/Forge.env/.env`. Gömülü anahtar APK'dan çıkarılabilir; bu tür APK'ları paylaşmayın. GitHub Actions derlemeleri anahtarsızdır.
