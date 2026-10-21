# નળીયાપરા પરિવાર – Android App

Native home screen (12 tiles) + WebView jo https://naliyapara.co.in ni website ma tab khole che.

## APK kevi rite banavvi (GitHub Actions)
1. Aa poora folder ne tamara GitHub repo ma push karo (alag repo ke website vala repo ma `android/` folder ma).
2. GitHub > Actions > "Build Android APK" > Run workflow (push par automatic chale).
3. Run purun thay pachi "Artifacts" ma `NaliyaparaParivar-apk` download karo > zip ma `app-debug.apk`.
4. APK mobile ma copy kari install karo ("Unknown sources" allow karo).

## Android Studio thi
File > Open > aa folder. Build > Build APK(s).

## Badlav
- Domain: `Config.kt`
- Tiles / tab mapping: `Tile.kt`
- App nu naam: `res/values/strings.xml`
