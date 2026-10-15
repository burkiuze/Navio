# Navio

Navio, iki modlu bir yapay zekâ asistanıdır (Android / Jetpack Compose):

- **Chat** — OpenRouter modelleriyle sohbet. Web araması OpenRouter'ın `web` eklentisiyle yapılır; yanıtların üstünde kaynak kartları gösterilir. Sohbette "her sabah 9'da … özetle" gibi bir istek yazarsanız yapay zekâ `create_automation` aracıyla otomasyonu kendisi kurar.
- **Computer** — Zamanlanmış otomasyonlar (saatlik / günlük / haftalık / elle). Her çalıştırmada talimat web aramasıyla yürütülür, sonuç kaydedilir ve bildirim olarak gelir (WorkManager). Otomasyonlar alttaki kutuya doğal dille yazılarak ya da `+` ile form üzerinden kurulabilir.

## API anahtarı (Forge.env)

Anahtar, derleme sırasında şu sırayla aranır ve `BuildConfig.OPENROUTER_KEY` olarak gömülür:

1. `OPENROUTER_API_KEY` ortam değişkeni
2. `Navio/.env`
3. `../Forge.env/.env` (Forge.env deposu Navio'nun yanına klonlanmışsa)
4. `Navio/Forge.env/.env`

```bash
git clone https://github.com/burkiuze/Navio
git clone https://github.com/burkiuze/Forge.env   # Navio ile aynı klasöre
cd Navio && ./gradlew assembleDebug
```

Anahtar uygulama içindeki **Ayarlar** sayfasından da girilebilir (Forge.env anahtarını geçersiz kılar). APK'ya gömülen anahtar çıkarılabilir; APK'yı herkese açık dağıtmayın.
