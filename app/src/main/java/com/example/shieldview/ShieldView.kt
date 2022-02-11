package com.example.shieldview.ui.theme

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import com.example.shieldview.*
import com.example.shieldview.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShieldView(
    modifier: Modifier = Modifier, onFinish: () -> Unit
) {
    val context = LocalContext.current
    val typeFace = ResourcesCompat.getFont(LocalContext.current, R.font.opensans_semibold)
    val pathShield = PathParser().parsePathString(PATH_SHIELD).toPath()
    val pathShade1 = PathParser().parsePathString(PATH_SHADE_1).toPath()
    val pathShade2 = PathParser().parsePathString(PATH_SHADE_2).toPath()
    val pathClip = PathParser().parsePathString(PATH_CLIP_PATH).toPath()

    var percent by remember {
        mutableStateOf(54)
    }

    val infinityTransition = rememberInfiniteTransition()

    val initScale by remember {
        mutableStateOf(2f)
    }
    val scaleValue by infinityTransition.animateFloat(
        initialValue = initScale,
        targetValue = 2.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val alphaValue by infinityTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color.White.copy(alpha = 0.0f),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        )
    )

    val coordinateAnimated by infinityTransition.animateFloat(
        initialValue = -90f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        )
    )

    var targetValue by remember {
        mutableStateOf(50f)
    }

    val sweepAnimated by infinityTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        )
    )

    val boundShield = pathShield.getBounds()
    val boundClip = pathClip.getBounds()
    val matrix = remember {
        Matrix()
    }


    Canvas(modifier) {
        //draw shield
        Log.d("ManhNQ", "ShieldView: $sweepAnimated")
        setupMatrix(matrix, initScale, boundShield) {
            drawShield(pathShield, pathShade1, pathShade2)
        }

        setupMatrix(matrix, initScale, boundClip) {
            clipPath(pathClip) {
//                drawArc(
//                    color = Color.White.copy(alpha = 0.4f),
//                    startAngle = coordinateAnimated,
//                    sweepAngle = sweepAnimated,
//                    useCenter = true,
//                    topLeft = boundClip.topLeft,
//                    size = boundClip.size
//                )

                drawArc(
                    color = Color.White.copy(alpha = 0.4f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = boundClip.topLeft,
                    size = boundClip.size
                )
            }


            drawArc(
                color = Color.Green.copy(alpha = 0.4f),
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = true,
                topLeft = boundClip.topLeft,
                size = boundClip.size
            )

        }
        drawPercent(context = context, percent = percent, typeFace = typeFace, boundShield)

        setupMatrix(matrix, scaleValue, boundShield) {
            drawPath(pathShield, color = alphaValue)
        }


    }
}

fun DrawScope.drawShield(pathShield: Path, pathShade1: Path, pathShade2: Path) {
    drawPath(pathShield, Color.White)
    drawPath(pathShade1, Color.White.copy(alpha = 0.5f))
    drawPath(pathShade2, Color.White.copy(alpha = 0.5f))
}

fun DrawScope.drawPercent(context: Context, percent: Int, typeFace: Typeface?, bound: Rect) {
    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#ffffff")
        style = Paint.Style.FILL
        textSize = context.dp2Px(30f).toFloat()
        typeface = typeFace
    }
    drawIntoCanvas {
        val rect = android.graphics.Rect()
        paint.getTextBounds("$percent%", 0, "$percent%".length, rect)

        it.nativeCanvas.apply {
            drawText(
                "$percent%",
                width / 2f - rect.width() / 2f,
                height / 2f + rect.height() / 2f,
                paint
            )
        }
    }

}


fun DrawScope.setupMatrix(matrix: Matrix, ratio: Float, bound: Rect, drawFunc: () -> Unit) {
    matrix.reset()
    matrix.scale(ratio, ratio)
    val offsetX = (size.width / (2f * ratio)) - (bound.width / 2f + bound.left)
    val offsetY = (size.height / (2f * ratio)) - (bound.height / 2f + bound.top)
    matrix.translate(x = offsetX, y = offsetY)

    withTransform(transformBlock = {
        transform(matrix)
    }, drawBlock = {
        drawFunc.invoke()
    })
}


@Composable
@Preview()
fun PreviewShield() {
    Box() {
        ShieldView(modifier = Modifier.fillMaxSize()) {
        }
    }
}


