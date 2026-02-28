package com.example.ra3p1

// Imports bàsics
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.delay

// Per arrodonir les vores
import androidx.compose.material3.*

// Estètica
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

// Imports de la llibreria COIL (per carregar imatges des de URL directament)
import com.example.ra3p1.ui.theme.RA3p1Theme

// Afegim això per poder passar d'una Activity a una altra
import android.content.Intent
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade

// Informació per ajudar a l'Intent
const val ID_DISC = "extra_disc_id"
const val TITLE_DISC = "extra_title"
const val BAND_NAME = "extra_band"
const val COVER_IMAGE = "extra_cover"

data class Disc(
    val id: Long,           // identificador del disc
    val title: String,      // títol del disc
    val band: String,       // artista/banda
    val coverUrl: String    // URL de la portada
)

// MainActivity és la porta d'entrada de l'app.

// Fem servir ComponentActivity. No fem servir XML:
// És a dir, tota la UI es defineix en Kotlin amb @Composable.

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent: aquí li diem a Android quina UI Compose ha de dibuixar
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// @Composable vol dir que és un element de la GUI
// (no és una funció normal).

@Composable
fun AppRoot() {  // Arrel de la GUI de l'app

    // Posem un Surface aquí perquè, en Compose amb Material 3,
    // és el contenidor correcte per definir l'àrea base de la pantalla:
    // fons, tema, i (si cal) comportaments visuals
    // com tema clar/fosc etc.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // remember{...} fa que el valor només es calculi un cop mentre el Composable
        // estigui "viu" (i no es recalculi a cada recomposició).
        val discs = remember { sampleDiscs() }

        // Pintem la graella amb la llista
        DiscsGrid(discs = discs)
    }
}

@Composable
fun DiscsGrid(discs: List<Disc>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),           // Graella de 3 columnes
        // Padding exterior (marge de tota la graella)
        contentPadding = PaddingValues(12.dp),

        // Espaiat vertical i horitzontal entre els elements
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),

        // Ocupa tota la pantalla disponible
        modifier = Modifier.fillMaxSize()
    ) {
        // Per cada disc → crea un "tile" / targeta
      /* items(discs) { disc ->
            DiscTitle(disc = disc)
        }*/

        itemsIndexed(discs) { index, disc ->
            DiscTitle(
                disc = disc,
                index = index
            )
        }
    }
}

// @Composable que dibuja UNA tarjeta (Card) que contiene:
// - portada cuadrada con rounded corners
// - loading spinner mientras carga
// - título y banda debajo

@Composable
//fun DiscTitle(disc: Disc) {
fun DiscTitle(disc: Disc, index:Int) {
    // Context Android (necesario para construir ImageRequest de Coil)
    val ctx = LocalContext.current

    // Forma para recortar la portada con cantos redondeados
    val shape = RoundedCornerShape(14.dp)

    var shown by remember(disc.coverUrl) { mutableStateOf(false) }

    // LaunchedEffect en cadena:
    val stepMs=150 // milisegons entre l'aparició d'un disc i el següent

    LaunchedEffect(disc.coverUrl){
        delay((index*stepMs).toLong())
        shown=true
    }

    val alpha by animateFloatAsState(
        targetValue=if(shown)1f else 0f,
        animationSpec=tween(durationMillis=1000),
        label="tileAlpha"
    )

    val scale by animateFloatAsState(
        targetValue=if(shown)1f else 0.1f,
        animationSpec=tween(durationMillis=500),
        label="tileScale"
    )

    // Construcción de la petición (request) de la imagen (Coil)
    // No queremos reconstruir la request en cada recomposición,
    // por eso usamos remember(...)
    val req = remember(disc.coverUrl) {
        ImageRequest.Builder(ctx)
            .data(disc.coverUrl)
            .crossfade(true)           // transición suave cuando la imagen aparece
            .allowHardware(false)      // evita algunos problemas de compatibilidad en algunos dispositivos
            .build()
    }

    // El Painter de Coil gestiona:
    // - descargar imagen de la red
    // - la caché
    // - el estado (loading/error/success)
    val painter = rememberAsyncImagePainter(model = req)
    val state = painter.state

    // GUI de la Card
    Card(
        modifier = Modifier
            .fillMaxWidth()

            // Poder fer click
            .clickable {
                val i = Intent(ctx, DiscDetailActivity::class.java).apply {
                    putExtra(ID_DISC, disc.id)
                    putExtra(TITLE_DISC, disc.title)
                    putExtra(BAND_NAME, disc.band)
                    putExtra(COVER_IMAGE, disc.coverUrl)
                }
                ctx.startActivity(i)
            }

            // AFEGIM alpha=alpha
            .graphicsLayer{
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }

            .animateContentSize()   // si aparecen elementos (spinner / texto error),
        // la Card ajusta la medida suavemente en lugar de un "salto" brusco
    ) {
        Column(modifier = Modifier.padding(8.dp)) {

            // Box nos permite superponer cosas:
            // - debajo: la imagen
            // - en medio: spinner loading
            // - encima: overlay de error + textos
            Box(
                modifier = Modifier.fillMaxWidth()
                    .aspectRatio(1f)           // cuadrado perfecto (altura = anchura)
                    .clip(shape)               // recorta el contenido con esquinas redondeadas
            ) {
                // Imagen de Compose + Coil
                Image(
                    painter = painter,
                    contentDescription = "${disc.title} - ${disc.band}",
                    contentScale = ContentScale.Crop,  // recorta para llenar el cuadrado (estilo Instagram)
                    modifier = Modifier.fillMaxSize()
                )

                // Estado LOADING: spinner centrado
                if (state is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(10.dp)
                    )
                }

                // ¿Qué pasa si hay error?
                if (state is AsyncImagePainter.State.Error) {
                    // Para simplificar no gestionamos el posible error
                    // de carga de la imagen en esta versión
                }
            }

// Espacio vertical entre la imagen y los textos
            Spacer(modifier = Modifier.height(8.dp))

// Título del disco
            Text(
                text = disc.title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,     // si es muy largo no rompe la tarjeta, pone "..."
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            // Nom de la banda
            Text(
                text = disc.band,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // Color más "suau" perquè sigui secundari respecte al títol
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// sampleDiscs() retorna una llista fixa de discs (dades d'exemple)

fun sampleDiscs(): List<Disc> {
    return listOf(
        // Eldamar 1
        Disc(
            id = 1,
            title = "The Force of the Ancient Land",
            band = "Eldamar",
            coverUrl = "https://f4.bcbits.com/img/a2579763197_10.jpg"
        ),

        // Eldamar 2
        Disc(
            id = 2,
            title = "Lost Songs from the Ancient Lands",
            band = "Eldamar",
            coverUrl = "https://f4.bcbits.com/img/a3483449309_10.jpg"
        ),

        Disc(
            id = 3,
            title = "Sol Y Sombra",
            band = "Rose City Band",
            coverUrl = "https://f4.bcbits.com/img/a2439766698_16.jpg"
        ),

        Disc(
            id = 4,
        title = "Garden Party",
        band = "Rose City Band",
        coverUrl = "https://f4.bcbits.com/img/a3004151730_16.jpg"
    ),

        Disc(
            id = 5,
            title = "Live at Moods",
            band = "Sonar",
            coverUrl = "https://f4.bcbits.com/img/a1092854074_10.jpg"
        ),

        Disc(
            id = 6,
            title = "Most Normal",
            band = "Gilla Band",
            coverUrl = "https://f4.bcbits.com/img/a3199713761_10.jpg"
        ),

        Disc(
            id = 7,
            title = "Holding Hands With Jamie",
            band = "Gilla Band",
            coverUrl = "https://f4.bcbits.com/img/a1652659123_10.jpg"
        ),

        Disc(
            id = 8,
            title = "Black Light",
            band = "Sonar",
            coverUrl = "https://f4.bcbits.com/img/a3895692563_10.jpg"
        ),

        Disc(
            id = 9,
            title = "Skeleton Groove",
            band = "Sonar",
            coverUrl = "https://f4.bcbits.com/img/a2170501674_16.jpg"
        ),

        Disc(
            id = 10,
            title = "Live in London",
            band = "Cathedral",
            coverUrl = "https://f4.bcbits.com/img/a3775085275_16.jpg"
        ),

        Disc(
            id = 11,
            title = "The Serpent's Gold",
            band = "Cathedral",
            coverUrl = "https://f4.bcbits.com/img/a3998866277_16.jpg"
        ),

        Disc(
            id = 12,
            title = "Endtyme",
            band = "Cathedral",
            coverUrl = "https://f4.bcbits.com/img/a3056941540_16.jpg"
        ),

        Disc(
            id = 13,
            title = "DUCK",
            band = "The Aristocrats",
            coverUrl = "https://f4.bcbits.com/img/a4067891844_16.jpg"
        ),

        Disc(
            id = 14,
            title = "FREEZE! Live In Europe 2020",
            band = "The Aristocrats",
            coverUrl = "https://f4.bcbits.com/img/a2818774629_16.jpg"
        ),

        Disc(
            id = 15,
            title = "Royal Bastards",
            band = "1968",
            coverUrl = "https://f4.bcbits.com/img/a0257197428_16.jpg"
        ),

        Disc(
            id = 16,
            title = "Salvation if you Need",
            band = "1968",
            coverUrl = "https://f4.bcbits.com/img/a3644719318_16.jpg"
        ),

    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RA3p1Theme {
        Greeting("Android")
    }
}