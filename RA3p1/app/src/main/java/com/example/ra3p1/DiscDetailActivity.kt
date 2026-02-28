package com.example.ra3p1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade


data class Track(
    val id: Long,
    val title: String,
    val durationMin: String,
    val durationSec: String
)

data class DiscDetail(
    val id: Long,
    val title: String,
    val band: String,
    val genre: String,
    val year: Int,
    val coverUrl: String,
    val tracks: List<Track>,
    val totalDuration: String
)

class DiscDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra(ID_DISC, -1L)
        val title = intent.getStringExtra(TITLE_DISC) ?: "Disc"
        val band = intent.getStringExtra(BAND_NAME) ?: "Artista"
        val cover = intent.getStringExtra(COVER_IMAGE) ?: ""

        val disc = demoDiscDetail(id, title, band, cover)

        setContent {
            MaterialTheme {
                DiscDetailScreen(disc = disc)
            }
        }
    }
}

@Composable
fun DebugExtrasScreen(
    id: Long,
    title: String?,
    band: String?,
    cover: String?
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "DEBUG EXTRAS",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "ID: $id")
        Text(text = "TITLE: $title")
        Text(text = "BAND: $band")
        Text(text = "COVER: $cover")
    }
}


private fun demoDiscDetail(
    id: Long,
    title: String,
    band: String,
    coverUrl: String
): DiscDetail {

    val tracks = listOf(
        Track(1, "Spirit of the North", "14", "46"),
        Track(2, "Winter Night", "11", "00"),
        Track(3, "Travel in Woods", "04", "45"),
        Track(4, "From Life to Spirit", "11", "57"),
        Track(5, "Valkyrjur Ancient One", "03", "08"),
        Track(6, "The Border of Eldamar", "13", "21"),
        Track(7, "Galaðwen The Eldamar", "06", "08"),
        Track(8, "New Beginning", "08", "54")
    )

    return DiscDetail(
        id = id,
        title = title,
        band = band,
        genre = "Atmospheric Black Metal",
        year = 2016,
        coverUrl = coverUrl,
        tracks = tracks,
        totalDuration = "01:14:01"
    )
}

@Composable
fun DiscDetailScreen(disc: DiscDetail) {
    val bg = Color(0xFF262626)
    val genreRed = Color(0xFFB64242)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bg
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = disc.title,
                    color = Color(0xFFFE8E8E),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = disc.band,
                    color = Color(0xFFCCCCCC),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = disc.genre,
                    color = genreRed,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Any de publicació: ${disc.year}",
                    color = Color(0xFFBDBDBD),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                DiscCover(url = disc.coverUrl)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pistes",
                    color = Color(0xFFFE0E0E0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(disc.tracks) { index, track ->
                TrackRow(
                    index = index + 1,   // normalment comencem des de 1
                    track = track
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duració total",
                        color = genreRed,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = disc.totalDuration,
                        color = Color(0xFFBDBDBD),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DiscCover(url: String) {
    val ctx = LocalContext.current
    val shape = RoundedCornerShape(10.dp)

    val req = remember(url) {
        ImageRequest.Builder(ctx)
            .data(url)
            .crossfade(true)
            .allowHardware(false)  // opcional: evita problemes amb certs dispositius
            .build()
    }

    val painter = rememberAsyncImagePainter(model = req)
    val state = painter.state

    Box(
        modifier = Modifier
            .size(210.dp)
            .clip(shape)
            .background(Color(0xFF1F1F1F)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = "Portada del disc",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TrackRow(
    index: Int,
    track: Track,
    modifier: Modifier = Modifier
) {
    val titleShape = RoundedCornerShape(4.dp)
    val playBg = Color(0xFFEEEEEE)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable { /* Aquí pots afegir l'acció de reproduir la pista */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(titleShape)
                .background(playBg)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(android.R.drawable.ic_media_play),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$index. ${track.title}",
            color = Color(0xFFE6E6E6),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "${track.durationMin}:${track.durationSec}",
            color = Color(0xFFE9E9E9),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}