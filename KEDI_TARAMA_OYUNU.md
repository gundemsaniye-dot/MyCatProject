# Kedi Tarama Oyunu — Codex Proje Talimatı

## Teknoloji

- Kotlin Multiplatform (KMP)
- Android ve iOS
- Compose Multiplatform ile ortak arayüz
- Oyun mantığı ve efektler `commonMain` içinde geliştirilecek

## Oyun Konsepti

Oyuncu, arkası dönük kediyi parmağıyla tarar. Tarama sırasında kedinin tüyleri dökülür. Kedi rastgele öne döndüğünde oyuncu taramaya devam ediyorsa yakalanır ve oyun biter.

## Ekranlar

### 1. Başlangıç Ekranı

- Oyun adı
- Kedi görseli
- **Oyunu Başlat** butonu

### 2. Oyun Ekranı

- Arkası dönük kedi
- Parmağın altında hareket eden tarak PNG'si
- Tarağı kedi üzerinde sürükleyerek tarama mekaniği
- Tüy parçacığı efekti
- Tarama ilerlemesi ve puan
- Kedi rastgele bir süre sonra öne döner

### 3. Yakalanma Animasyonu

- Kedi öne dönük görsele geçer
- Kafa bölgesi ağzı açık kedi kafasıyla değiştirilir
- Açık ağızlı kafa büyüyerek ekrana yaklaşır
- Ses ve titreşim oynatılır

### 4. Game Over Ekranı

- **GAME OVER** yazısı
- Kazanılan puan
- **Tekrar Oyna** ve **Ana Menü** butonları

## Kedi Sistemi

Her kedi için:

- Arkası dönük/bekleyen kedi görseli
- Öne dönük kedi görseli
- Ağzı açık, şeffaf kedi kafası görseli
- Bir veya birden fazla tüy rengi

Her yeni oyunda listeden rastgele bir kedi seçilecek. Seçilen kedinin üç görseli ve tüy renk paleti birlikte kullanılacak.

## Tüy Efekti

- Tüyler için PNG kullanılmayacak.
- Efekt Compose `Canvas` üzerinde programatik çizilecek.
- Her tüy ince ve hafif eğri bir `Path` olacak.
- Uzunluk, kalınlık, eğrilik, dönüş ve hız rastgele değişecek.
- Renkler seçilen kedinin tüy paletinden alınacak.
- Tüyler önce hafifçe yukarı çıkacak, sonra yer çekimiyle aşağı süzülecek.
- Zamanla şeffaflaşan tüyler bellekten silinecek.
- Aynı anda en fazla 100–120 tüy gösterilecek.

## Tarama Mekaniği

- `pointerInput` ve `detectDragGestures` kullanılacak.
- Oyuncu ekrana dokunduğunda `comb.webp` tarak görseli parmak konumuna gelecek.
- Tarak PNG'si sürükleme boyunca parmağı takip edecek ve hareket yönüne göre hafifçe dönecek.
- Tarağın dişlerinin bulunduğu alt bölüm gerçek temas noktası kabul edilecek.
- Yalnızca tarağın temas noktası kedinin taranabilir alanındaysa tüy üretilecek ve puan artacak.
- Tüyler parmak merkezinden değil, tarağın dişlerinin kediye temas ettiği noktadan çıkacak.
- Parmağın kaldırılmasıyla tarak gizlenecek veya başlangıç konumuna dönecek.
- Aynı noktayı sürekli tarayarak sınırsız puan kazanılması engellenecek.
- Çok kısa veya hareketsiz dokunuşlar tarama sayılmayacak; minimum sürükleme mesafesi aranacak.
- İlk sürümde kedi sınır alanı kullanılabilir; daha sonra hassas hit-mask eklenebilir.
- Kedi öne döndüğünde tarama sürüyorsa oyuncu yakalanacak.
- Oyuncu parmağını zamanında kaldırırsa kedi tekrar arkasını dönecek.
- Tarama hedefi tamamen doldurulursa bölüm kazanılacak.

## Oyun Durumları

```text
START
PLAYING
CAT_TURNED
CAUGHT
GAME_OVER
WIN
```

Akışlar:

```text
START → PLAYING → CAT_TURNED → CAUGHT → GAME_OVER
START → PLAYING → CAT_TURNED → PLAYING
START → PLAYING → WIN
```

## Görsel Katmanlar

```text
Box
├── Arka plan
├── Kedi görseli
├── Canvas tüy parçacıkları
├── Parmağı takip eden tarak PNG'si
├── Puan ve ilerleme göstergesi
└── Yakalanma / Game Over katmanı
```

## Gerekli Asset İsimleri

Her kedi için:

```text
cat_01_back.webp
cat_01_front.webp
cat_01_mouth.webp
```

Ayrıca:

```text
background.webp
comb.webp
brush_sound.*
cat_turn_sound.*
game_over_sound.*
```

## Tüm Ekranlara Uyum Araştırması (Android, iOS ve Web)

### Karar

Arayüz, cihaz modeline ("telefon", "tablet" veya belirli bir iPhone modeli) göre değil, uygulamanın o anda kullanabildiği **pencere alanına** göre uyarlanmalıdır. Böylece portre/yatay kullanım, bölünmüş ekran, katlanabilir Android cihazlar, küçük iPhone'lar ve büyük iPad ekranları tek ortak Compose koduyla desteklenir.

Bu proje için önerilen teknoloji:

- Ortak arayüz: **Compose Multiplatform** (`shared/src/commonMain`)
- Uygulama-geneli kırılım noktaları: **Material 3 Adaptive / `WindowSizeClass`**
- Bileşen-geneli esneklik: `fillMaxWidth`, `weight`, `FlowRow`, `Lazy*` bileşenleri ve gerektiğinde `BoxWithConstraints`
- Güvenli alanlar: `safeContentPadding()` ve uygun pencere/klavye inset'leri
- Geniş ekran düzeni: Material 3'ün list-detail, feed ve supporting-pane kalıpları

JetBrains'in güncel dokümanına göre `WindowSizeClass`, kullanılabilir pencere alanını `compact`, `medium` ve `expanded` olarak sınıflandırır. Ortak kaynak kümesindeki bağımlılık şu şekildedir:

```kotlin
commonMain.dependencies {
    implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.3.0-beta02")
}
```

Uygulamanın üst seviyesinde pencere sınıfı bir kez hesaplanmalı ve ekranlara açık parametre olarak aktarılmalıdır:

```kotlin
@Composable
fun CatGameApp() {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    GameRouter(windowSizeClass = windowSizeClass)
}
```

Bu yaklaşımda tekil kartlar veya oyun içi katmanlar global ekran boyutunu doğrudan okumaz; kendi kaplarına verilen alanla esnekleşir. Bu, bileşenlerin farklı ekranlarda ve farklı navigasyon düzenlerinde tekrar kullanılmasını sağlar.

### Bu Oyun İçin Uyumluluk Kuralları

| Alan | Compact (telefon, dar pencere) | Medium / Expanded (yatay telefon, tablet, geniş pencere) |
| --- | --- | --- |
| Başlangıç ve Game Over | Dikey akış; görsel sınırlı yükseklikte, butonlar tam genişlikte | Görsel ve metin yan yana veya ortalanmış geniş bir sütunda; butonların maksimum genişliği sınırlı |
| Oyun sahnesi | Kedi, tarama alanı ve HUD tek elde erişilebilir kalır; skor üstte ve güvenli alan içinde | Sahne merkezde sabit en-boy oranını korur; ek boşluk skor, ipucu veya kedi seçimi için kullanılır |
| Tarak etkileşimi | Dokunma hedefleri en az 48 dp; parmağın altında tarak konumu korunur | Tarak/kedi koordinatları sahnenin gerçek boyutundan türetilir; sabit piksel koordinatı kullanılmaz |
| Puan ve ilerleme | Üstte küçük, tek satırlı HUD; önemli içerik klavyeden ve çentikten uzak | Yan panel veya geniş üst şerit kullanılabilir |
| Yakalanma animasyonu | Ağzı açık kafa ekranı kaplayabilir fakat safe-area dışına taşmamalı | Animasyon sahne merkezine göre ölçeklenmeli; tüm ekranı kontrolsüz kaplamamalı |
| Navigasyon | Alt navigasyon veya tek ekran akışı | Navigation rail ya da yan panel yalnızca gerçekten birden fazla ana bölüm varsa |

### Oyun Sahnesi İçin Teknik Kurallar

1. Kedi ve tarak koordinatları tasarım pikseliyle saklanmamalı; `Canvas`/yerleşim boyutundan hesaplanan normalize edilmiş koordinatlar (`0f..1f`) kullanılmalıdır. Böylece aynı tarama hit-area'sı her en-boy oranında doğru ölçeklenir.
2. Kedi görseli `ContentScale.Fit` ile sahne sınırlarına oturmalı; taranabilir alan görselin ekrandaki gerçek `Rect` değerinden üretilmelidir.
3. Tüy parçacıklarının hızı, boyutu ve çıkış konumu sahne ölçeğiyle çarpılmalıdır. Aynı anda 100–120 tüy sınırı ekran boyutundan bağımsız kalabilir.
4. Butonlar ve kritik kontroller sabit `px` yerine `dp` ile, metinler `sp` ile tanımlanmalı; uzun çeviri/metin için taşma stratejisi kullanılmalıdır.
5. Yatay kullanımda oyun alanı daralırsa HUD sıkıştırılmamalı; üst HUD iki satıra geçmeli veya bilgi ikincil alana taşınmalıdır.
6. `safeContentPadding()` başlangıç için kullanılmalı; metin girişi olan gelecekteki ekranlarda klavye açıldığında önemli kontrollerin kapanmadığı ayrıca gözetilmelidir.

### Web Test Politikası

- Geliştirme ve görsel doğrulama yalnızca `webApp[wasmJs]` üzerinde yapılır.
- Web sunucusu komutu: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
- Testte en az dar telefon, geniş telefon ve tablet benzeri üç viewport denenmelidir. Her görünümde başlangıç, oyun, yakalanma, Game Over ve kazanma ekranları kontrol edilmelidir.
- Bu yaklaşım ortak Compose yerleşimi, ölçekleme ve etkileşim mantığındaki hataları yakalar.
- Web testi, iOS'a özgü klavye, safe-area ve sistem davranışlarını kesin olarak doğrulamaz. Compose Multiplatform iOS safe area için Compose window-insets yaklaşımını uygular; bu nedenle ortak kodda safe-area/inset kurallarına uyulmalıdır.

### Resmî Kaynaklar

- JetBrains Kotlin Multiplatform: [Adaptive layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
- JetBrains Kotlin Multiplatform: [Platform-specific UI behavior](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
- Android Developers: [Support different display sizes](https://developer.android.com/develop/adaptive-apps/guides/support-different-display-sizes)
- Android Developers: [Build adaptive apps with Compose](https://developer.android.com/develop/ui/compose/build-adaptive-apps)

## Codex Görevi

Çalışan bir Kotlin Multiplatform projesi oluştur. Android ve iOS arayüzünü Compose Multiplatform ile paylaş. Başlangıç, oyun, yakalanma, Game Over ve kazanma akışlarını uygula. Rastgele kedi seçimini, parmağı takip eden `comb.webp` tarak PNG'sini, tarağın dişleri üzerinden temas kontrolünü, sürükleme algılamasını, tarama ilerlemesini, rastgele dönüş zamanını, Canvas tabanlı programatik tüy parçacık sistemini, kafa büyütme animasyonunu, ses/titreşimi ve tekrar oynama özelliğini eksiksiz geliştir. Gerçek assetler hazır değilse aynı isimlerle geçici görseller kullan.
