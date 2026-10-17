# WhatsApp Wallpaper Pro Implementation Plan

**Goal:** تطبيق محلي يجهز أي صورة كخلفية دون قص تلقائي.
**Architecture:** Compose UI → ViewModel → ImageProcessor/Exporter، مع نماذج حالة غير قابلة للتعديل.
**Tech Stack:** Kotlin, Compose Material 3, Android Photo Picker, MediaStore.
**Spec:** docs/SPEC.md

## المهام
- [x] تهيئة Gradle وManifest وRTL.
- [x] بناء نماذج المقاسات وخيارات الخلفية.
- [x] تنفيذ فك ترميز الصور وتدوير EXIF والاحتواء والخلفية الذكية.
- [x] بناء شاشة الاختيار والمعاينة والتحرير والتصدير.
- [ ] تشغيل Android SDK فعليًا وبناء Debug APK عند توفير SDK/Gradle.
- [ ] اختبار يدوي على Honor X9c Smart وAndroid 15.
