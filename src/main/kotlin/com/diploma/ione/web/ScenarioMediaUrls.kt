package com.diploma.ione.web

/** Нормализует путь картинки/видео: внешние URL без префикса /media/, файлы — относительный /media/... */
object ScenarioMediaUrls {
    fun resolve(path: String?): String? {
        if (path.isNullOrBlank()) return null
        var t = path.trim()
        if (t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true)) {
            return t
        }
        val mediaHttp = t.indexOf("/media/http")
        if (mediaHttp != -1) {
            return t.substring(mediaHttp + 7)
        }
        val mediaHttps = t.indexOf("/media/https")
        if (mediaHttps != -1) {
            return t.substring(mediaHttps + 7)
        }
        val wwwIdx = t.indexOf("/media/www.")
        if (wwwIdx != -1) {
            return "https://" + t.substring(wwwIdx + 7)
        }
        val noLeading = t.removePrefix("/")
        return "/media/$noLeading"
    }
}
