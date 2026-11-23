# Kioskio

Aplikasi Android untuk pemeliharaan dan pengendalian mesin vending dengan 45 motor spiral.

## Identitas
- App: Kioskio
- Application ID: `dev.andikune.kioskio`
- UI: Kotlin + Jetpack Compose
- Min SDK: 26

## Status implementasi
- [x] Fondasi proyek Android dan layar pemeliharaan awal.
- [x] Daftar pemilihan motor 01–45.
- [x] Input nomor motor.
- [ ] Identifikasi terminal Android dan antarmuka controller.
- [ ] Implementasi protokol motor dan verifikasi feedback.
- [ ] Remote dashboard LAN dan lifecycle server.
- [ ] Crash recovery, update checker, dan structured storage.
- [ ] Pengujian perangkat dan validasi build.

## Keselamatan perangkat
Controller, transport, protokol, dan feedback motor belum diketahui. Karena itu, tombol pengujian fisik sengaja dinonaktifkan. UI tidak mensimulasikan motor bergerak dan tidak mengirim perintah sampai jalur komunikasi nyata diverifikasi.

Remote control juga belum dibuka. Akses LAN tanpa autentikasi dapat memungkinkan perangkat lain pada jaringan yang sama menggerakkan motor.

## Pengembangan
Buka proyek menggunakan Android Studio dengan JDK 17 dan Gradle Wrapper yang sesuai. Jalankan unit test dan build debug sebelum pemasangan perangkat.

Ikuti [AI Rulebook V3](https://github.com/andikune-ux/ai-rulebook) dan instruksi proyek di [AGENTS.md](AGENTS.md).
