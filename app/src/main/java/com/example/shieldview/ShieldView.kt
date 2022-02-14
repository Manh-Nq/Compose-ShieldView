package com.example.shieldview

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShieldView(
    modifier: Modifier = Modifier,
    process: Double = 0.0,
    scanColor: Color = Color.Green,
    repeatDuration: Int = 1500,
    onFinishDone: () -> Unit
) {
    val context = LocalContext.current
    val typeFace = ResourcesCompat.getFont(LocalContext.current, R.font.opensans_semibold)
    val pathShield = PathParser().parsePathString(PATH_SHIELD).toPath()
    val pathShade1 = PathParser().parsePathString(PATH_SHADE_1).toPath()
    val pathShade2 = PathParser().parsePathString(PATH_SHADE_2).toPath()
    val pathClip = PathParser().parsePathString(PATH_CLIP_PATH).toPath()

    val delayTimeSweep = remember { repeatDuration }


    //animation repeat
    val infinityTransition = rememberInfiniteTransition()

    val scaleAnimated by infinityTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = delayTimeSweep,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val alphaAnimated by infinityTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color.White.copy(alpha = 0.0f),
        animationSpec = infiniteRepeatable(
            animation = tween(delayTimeSweep),
            repeatMode = RepeatMode.Restart
        )
    )

    val strokeAnimated by infinityTransition.animateFloat(
        initialValue = 10f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = delayTimeSweep,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val sweepAnimated by infinityTransition.animateFloat(

        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(delayTimeSweep),
            repeatMode = RepeatMode.Restart
        )
    )

    val boundShield = pathShield.getBounds()
    val boundClip = pathClip.getBounds()
    val matrix = remember {
        Matrix()
    }
    Canvas(modifier.background(scanColor)) {

        val percent = (process * 100).toInt()

        val weightParent = size.width.coerceAtMost(size.height)
        val weightView = boundShield.width.coerceAtMost(boundShield.height)

        val dpi = weightParent / (1.5f * weightView)

        setupMatrix(matrix, dpi, boundShield) {
            drawShield(pathShield, pathShade1, pathShade2)
            drawPercent(context = context, percent = percent, typeFace = typeFace, boundShield)
        }

        setupMatrix(matrix, dpi, boundClip) {
            drawProgressWave(
                boundRect = boundClip,
                color = Color.White.copy(alpha = 0.5f),
                pathClip = pathClip,
                angle = sweepAnimated
            )
        }
        setupMatrix(matrix = matrix, dpi, boundClip) {
            scale(
                scaleX = scaleAnimated,
                scaleY = scaleAnimated,
                pivot = boundClip.center
            ) {
                drawPath(pathClip, color = alphaAnimated, style = Stroke(strokeAnimated))
            }
        }
        if (process == 1.0) {
            onFinishDone.invoke()
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
        textSize = context.dp2Px(14f).toFloat()
        typeface = typeFace
    }
    drawIntoCanvas {
        val rect = android.graphics.Rect()
        paint.getTextBounds("$percent%", 0, "$percent%".length, rect)

        it.nativeCanvas.apply {
            drawText(
                "$percent%",
                bound.width / 2f - rect.width() / 2f,
                bound.height / 2f + rect.height() / 2f,
                paint
            )
        }
    }

}


fun DrawScope.setupMatrix(matrix: Matrix, ratio: Float, rectBound: Rect, drawFunc: () -> Unit) {
    matrix.reset()
    matrix.scale(ratio, ratio)
    val offsetX = (size.width / (2f * ratio)) - (rectBound.width / 2f + rectBound.left)
    val offsetY = (size.height / (2f * ratio)) - (rectBound.height / 2f + rectBound.top)
    matrix.translate(x = offsetX, y = offsetY)

    withTransform(transformBlock = {
        transform(matrix)
    }, drawBlock = {
        drawFunc.invoke()
    })
}

private fun DrawScope.drawProgressWave(
    angle: Float,
    pathClip: Path,
    color: Color,
    boundRect: Rect
) {
    val sizeOffset = boundRect.width.coerceAtMost(boundRect.height)

    val coordinateAngle = if (angle < 180)
        angle / 3f
    else
        convertValue(angle, 180f, 360f, 60f, 360f)

    val startAngle = coordinateAngle - 90
    val sweepAngle = angle - coordinateAngle

    val paramOffsetMax = boundRect.width.coerceAtLeast(boundRect.height)
    val paramOffsetMin = boundRect.width.coerceAtMost(boundRect.height)

    val offset =paramOffsetMax-paramOffsetMin

    Log.d("ManhNQ", "drawProgressWave: width: ${boundRect.width} -- height: ${boundRect.height}")

    clipPath(pathClip) {
        drawArc(
            color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(boundRect.top -offset, boundRect.left - offset),
            size = Size(boundRect.width + offset*2, boundRect.height + offset*2),
        )
    }
}


@Composable
@Preview()
fun PreviewShield() {
    Box {
        ShieldView(modifier = Modifier.fillMaxSize(), process = 0.0) {
        }
    }
}


const val PATH_SHIELD =
    "M215.865,31.266C181.852,25.51 149.016,15.829 117.488,1.57C115.264,0.523 113.17,0 110.946,0C108.853,0 106.629,0.523 104.405,1.57C73.008,15.829 40.172,25.51 6.158,31.266C1.841,32.051 0.01,33.621 0.01,38.461C-0.252,74.699 4.719,110.152 17.802,144.035C34.808,188.645 62.673,224.359 104.798,248.168C107.022,249.477 109.115,250 111.077,250C113.04,250 115.133,249.346 117.357,248.168C159.481,224.359 187.215,188.645 204.353,144.035C217.304,110.021 222.276,74.699 222.145,38.461C222.014,33.621 220.313,32.051 215.865,31.266ZM214.688,64.103C212.726,94.061 206.577,123.103 194.672,150.706C178.581,187.86 154.248,218.08 119.45,239.665C116.31,241.627 113.694,242.543 110.946,242.543C108.199,242.543 105.583,241.627 102.443,239.665C67.775,218.08 43.312,187.991 27.221,150.706C15.447,123.103 9.298,94.191 7.336,64.103C6.812,56.907 6.289,49.843 6.158,42.517C6.158,39.116 7.205,37.546 10.868,36.892C44.358,30.612 76.802,21.193 107.807,7.064C108.984,6.541 110.031,6.41 111.077,6.541C112.124,6.541 113.04,6.672 114.217,7.195C145.222,21.324 177.796,30.874 211.156,37.022C214.688,37.677 215.865,39.116 215.865,42.648C215.735,49.843 215.211,56.907 214.688,64.103Z"
const val PATH_SHADE_1 =
    "M63.589,172.945C97.472,172.029 126.383,161.433 145.745,131.606C150.847,123.756 154.772,115.253 157.519,106.226C158.696,102.171 160.659,100.601 165.106,101.909C174.002,104.395 183.029,105.049 192.317,105.31C196.504,105.441 198.858,107.011 197.55,111.851C185.253,159.994 161.313,200.287 119.057,228.283C113.432,232.076 109.115,232.207 103.228,228.413C83.474,215.331 67.514,198.586 54.039,179.355C52.992,177.786 50.899,176.216 51.815,174.253C52.731,172.029 55.216,173.207 57.048,173.076C59.141,172.814 61.365,172.945 63.589,172.945Z"
const val PATH_SHADE_2 =
    "M205.923,57.823C205.269,63.71 204.353,73.26 203.437,82.81C203.176,86.211 201.475,87.781 197.943,87.781C187.477,87.912 177.142,86.604 166.807,84.38C164.06,83.726 162.752,82.548 163.013,79.409C164.322,66.85 165.368,54.291 164.06,41.601C163.406,35.845 165.761,34.929 170.994,36.368C180.805,39.377 190.748,41.601 200.821,43.432C206.185,44.479 206.185,44.741 205.923,57.823Z"
const val PATH_CLIP_PATH =
    "M41.35,71.472C76.586,68.497 128.82,47.918 150.533,38C200.162,63.414 252.272,70.232 259.716,71.472C262.818,222.096 167.283,281.602 150.533,286.561C36.387,232.014 41.35,75.191 41.35,71.472Z"

fun Context.dp2Px(dp: Float): Int {
    return (dp * resources.displayMetrics.density + 0.5f).toInt()
}

fun convertValue(value: Float, min1: Float, max1: Float, min2: Float, max2: Float): Float {
    return ((value - min1) * ((max2 - min2) / (max1 - min1)) + min2)
}

