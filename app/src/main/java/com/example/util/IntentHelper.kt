package com.example.util

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object IntentHelper {

    /**
     * Opens the streaming platform with the highest specificity possible:
     * 1. Exact official movie deep link / universal link -> open official app
     * 2. Exact official web movie URL -> open browser directly to the movie
     * 3. Official platform deep link / app search with movie title pre-filled -> open app
     * 4. Official platform web search with movie title pre-filled -> open browser
     *
     * The user is NEVER left at a blank or generic search screen.
     */
    fun openStreamingPlatform(
        context: Context,
        platform: String,
        title: String,
        mediaType: String = "Movie",
        directWebLink: String? = null
    ) {
        val cleanTitle = title.trim()
        val encodedTitle = try {
            URLEncoder.encode(cleanTitle, "UTF-8")
        } catch (_: Exception) {
            cleanTitle.replace(" ", "%20")
        }

        try {
            Toast.makeText(context, "Opening $platform for \"$cleanTitle\"...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}

        val hasDirectLink = !directWebLink.isNullOrBlank() &&
                (directWebLink.startsWith("http://", ignoreCase = true) || directWebLink.startsWith("https://", ignoreCase = true))

        when {
            // 1. YouTube
            platform.contains("YouTube", ignoreCase = true) -> {
                if (hasDirectLink && (directWebLink!!.contains("youtube.com") || directWebLink.contains("youtu.be"))) {
                    val ytPkg = "com.google.android.youtube"
                    if (isPackageInstalled(context, ytPkg)) {
                        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directWebLink)).apply {
                            setPackage(ytPkg)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (tryLaunchIntent(context, appIntent)) return
                    }
                    openWebUrl(context, directWebLink)
                    return
                }

                // If no exact video link, search query in YouTube App or Web
                val appUriString = "vnd.youtube://results?search_query=$encodedTitle"
                val webUrl = "https://www.youtube.com/results?search_query=$encodedTitle"
                if (!tryOpenUri(context, appUriString)) {
                    openWebUrl(context, webUrl)
                }
            }

            // 2. Netflix
            platform.contains("Netflix", ignoreCase = true) -> {
                val netflixPkg = "com.netflix.mediaclient"
                val isInstalled = isPackageInstalled(context, netflixPkg)

                // If exact title direct link exists (e.g. netflix.com/title/...)
                if (hasDirectLink && directWebLink!!.contains("netflix.com", ignoreCase = true)) {
                    if (isInstalled) {
                        val directAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directWebLink)).apply {
                            setPackage(netflixPkg)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (tryLaunchIntent(context, directAppIntent)) return
                    }
                    openWebUrl(context, directWebLink)
                    return
                }

                if (isInstalled) {
                    // Try Netflix native search intent with query pre-filled
                    val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                        setPackage(netflixPkg)
                        putExtra("query", cleanTitle)
                        putExtra(SearchManager.QUERY, cleanTitle)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, searchIntent)) return

                    // Try Netflix deep link with query pre-filled
                    val appViewIntent = Intent(Intent.ACTION_VIEW, Uri.parse("nflx://www.netflix.com/search?q=$encodedTitle")).apply {
                        setPackage(netflixPkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, appViewIntent)) return

                    val appWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.netflix.com/search?q=$encodedTitle")).apply {
                        setPackage(netflixPkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, appWebIntent)) return
                }

                // Official web destination with title query pre-filled
                val netflixWeb = "https://www.netflix.com/search?q=$encodedTitle"
                openWebUrl(context, netflixWeb)
            }

            // 3. Amazon Prime Video
            platform.contains("Prime", ignoreCase = true) || platform.contains("Amazon", ignoreCase = true) -> {
                val primePkg = "com.amazon.avod.thirdpartyclient"
                val isInstalled = isPackageInstalled(context, primePkg)

                if (hasDirectLink && (directWebLink!!.contains("primevideo.com", ignoreCase = true) || directWebLink.contains("amazon.", ignoreCase = true))) {
                    if (isInstalled) {
                        val directAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directWebLink)).apply {
                            setPackage(primePkg)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (tryLaunchIntent(context, directAppIntent)) return
                    }
                    openWebUrl(context, directWebLink)
                    return
                }

                if (isInstalled) {
                    val appViewIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://app.primevideo.com/search?phrase=$encodedTitle")).apply {
                        setPackage(primePkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, appViewIntent)) return

                    val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                        setPackage(primePkg)
                        putExtra("query", cleanTitle)
                        putExtra(SearchManager.QUERY, cleanTitle)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, searchIntent)) return
                }

                val primeWeb = "https://www.primevideo.com/search/ref=atv_nb_sr?phrase=$encodedTitle"
                openWebUrl(context, primeWeb)
            }

            // 4. JioHotstar / Disney+ Hotstar
            platform.contains("Hotstar", ignoreCase = true) || platform.contains("Jio", ignoreCase = true) || platform.contains("Disney", ignoreCase = true) -> {
                val hotstarPkg = "in.startv.hotstar"
                val isInstalled = isPackageInstalled(context, hotstarPkg)

                if (hasDirectLink && directWebLink!!.contains("hotstar.com", ignoreCase = true)) {
                    if (isInstalled) {
                        val directAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directWebLink)).apply {
                            setPackage(hotstarPkg)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (tryLaunchIntent(context, directAppIntent)) return
                    }
                    openWebUrl(context, directWebLink)
                    return
                }

                if (isInstalled) {
                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("hotstar://search?q=$encodedTitle")).apply {
                        setPackage(hotstarPkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, appIntent)) return
                }

                val hotstarWeb = "https://www.hotstar.com/in/search?q=$encodedTitle"
                openWebUrl(context, hotstarWeb)
            }

            // 5. Apple TV
            platform.contains("Apple", ignoreCase = true) -> {
                if (hasDirectLink && directWebLink!!.contains("apple.com", ignoreCase = true)) {
                    openWebUrl(context, directWebLink)
                    return
                }
                val appleWeb = "https://tv.apple.com/search?term=$encodedTitle"
                openWebUrl(context, appleWeb)
            }

            // 6. Google Play Movies / Google TV
            platform.contains("Google", ignoreCase = true) -> {
                if (hasDirectLink && (directWebLink!!.contains("play.google.com") || directWebLink.contains("google.com"))) {
                    openWebUrl(context, directWebLink)
                    return
                }
                val playStoreUri = "market://search?q=$encodedTitle&c=movies"
                if (!tryOpenUri(context, playStoreUri)) {
                    openWebUrl(context, "https://play.google.com/store/search?q=$encodedTitle&c=movies")
                }
            }

            // 7. Zee5
            platform.contains("Zee", ignoreCase = true) -> {
                val zeePkg = "com.graymatrix.did"
                val isInstalled = isPackageInstalled(context, zeePkg)

                if (hasDirectLink && directWebLink!!.contains("zee5.com", ignoreCase = true)) {
                    if (isInstalled) {
                        val directAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directWebLink)).apply {
                            setPackage(zeePkg)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (tryLaunchIntent(context, directAppIntent)) return
                    }
                    openWebUrl(context, directWebLink)
                    return
                }

                if (isInstalled) {
                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("zee5://search?q=$encodedTitle")).apply {
                        setPackage(zeePkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, appIntent)) return
                }
                openWebUrl(context, "https://www.zee5.com/search?q=$encodedTitle")
            }

            // 8. Sony LIV
            platform.contains("Sony", ignoreCase = true) -> {
                val sonyPkg = "com.sonyliv"
                val isInstalled = isPackageInstalled(context, sonyPkg)

                if (hasDirectLink && directWebLink!!.contains("sonyliv.com", ignoreCase = true)) {
                    if (isInstalled) {
                        val directAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directWebLink)).apply {
                            setPackage(sonyPkg)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (tryLaunchIntent(context, directAppIntent)) return
                    }
                    openWebUrl(context, directWebLink)
                    return
                }

                if (isInstalled) {
                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sonyliv://search?q=$encodedTitle")).apply {
                        setPackage(sonyPkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (tryLaunchIntent(context, appIntent)) return
                }
                openWebUrl(context, "https://www.sonyliv.com/search?q=$encodedTitle")
            }

            // Fallback for other providers
            else -> {
                if (hasDirectLink) {
                    openWebUrl(context, directWebLink!!)
                } else {
                    val query = try {
                        URLEncoder.encode("watch $cleanTitle on $platform", "UTF-8")
                    } catch (_: Exception) {
                        cleanTitle
                    }
                    openWebUrl(context, "https://www.google.com/search?q=$query")
                }
            }
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun tryLaunchIntent(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun tryOpenUri(context: Context, uriString: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun openWebUrl(context: Context, url: String) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {
            try {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                    "Open streaming provider"
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                context.startActivity(chooser)
            } catch (_: Exception) {}
        }
    }
}
