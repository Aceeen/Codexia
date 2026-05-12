import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

fun main() {
    val query = "novel cover"
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val googleUrl = URL("https://www.google.com/search?q=$encodedQuery&tbm=isch&gbv=1")
    val connection = googleUrl.openConnection() as HttpURLConnection
    connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)")
    connection.connectTimeout = 5000
    if (connection.responseCode == 200) {
        val html = connection.inputStream.bufferedReader().use { it.readText() }
        val pattern = java.util.regex.Pattern.compile("<img[^>]*src=[\"']([^\"']+)[\"']")
        val matcher = pattern.matcher(html)
        var count = 0
        while (matcher.find()) {
            count++
            val url = matcher.group(1)
            println("Found image: $url")
        }
        println("Total found: $count")
        
        // Let's also check if there are other URLs like encrypted-tbn
        val pattern2 = java.util.regex.Pattern.compile("https://encrypted-tbn[^\"]+")
        val matcher2 = pattern2.matcher(html)
        var count2 = 0
        while (matcher2.find()) {
            count2++
            println("Found encrypted: " + matcher2.group())
        }
        println("Total encrypted found: $count2")
    } else {
        println("Response code: " + connection.responseCode)
    }
}
