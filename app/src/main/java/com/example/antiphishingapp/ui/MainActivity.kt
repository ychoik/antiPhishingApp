package com.example.antiphishingapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.network.ApiClient
import com.example.antiphishingapp.theme.AntiPhishingAppTheme
import com.example.antiphishingapp.utils.bitmapToMultipart
import org.opencv.android.OpenCVLoader
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "이미지 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ OpenCV 초기화 (박스 그리기용)
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV 초기화 실패")
        }

        // ✅ 권한 요청
        checkImagePermission()

        setContent {
            AntiPhishingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhishingDetectScreen()
                }
            }
        }
    }

    private fun checkImagePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("PERMISSION", "이미지 접근 권한 허용됨")
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

@Composable
fun PhishingDetectScreen() {
    var bitmapPreview by remember { mutableStateOf<Bitmap?>(null) }
    var serverResult by remember { mutableStateOf<AnalysisResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                if (inputStream == null) {
                    Toast.makeText(context, "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                    return@let
                }

                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmapPreview = bitmap
                val part = bitmapToMultipart(bitmap)
                val api = ApiClient.apiService

                isLoading = true

                // ✅ 서버 분석 요청 (/process-request)
                api.processRequest(part).enqueue(object : Callback<AnalysisResponse> {
                    override fun onResponse(
                        call: Call<AnalysisResponse>,
                        response: Response<AnalysisResponse>
                    ) {
                        isLoading = false
                        if (response.isSuccessful) {
                            val result = response.body()
                            if (result != null) {
                                serverResult = result
                                Log.d("PROCESS", "서버 분석 완료: 위험도=${result.final_risk}")
                                Toast.makeText(context, "서버 분석 완료!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("PROCESS", "분석 실패: ${response.code()}")
                            Toast.makeText(context, "분석 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AnalysisResponse>, t: Throwable) {
                        isLoading = false
                        Log.e("PROCESS", "서버 오류: ${t.message}")
                        Toast.makeText(context, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            } catch (e: Exception) {
                Log.e("GALLERY", "이미지 처리 중 오류: ${e.message}")
                Toast.makeText(context, "이미지 처리 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("서버 분석 중입니다...")
            }

            serverResult != null && bitmapPreview != null -> {
                val result = serverResult!!
                Text(
                    text = "📊 서버 분석 결과",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnalyzedImageWithServerBoxes(
                    bitmap = bitmapPreview!!,
                    boxes = result.stamp.boxes.map {
                        org.opencv.core.Rect(it.x, it.y, it.width, it.height)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("스탬프 개수: ${result.stamp.count}")
                Text("레이아웃 점수: ${result.layout.score}")
                Text("최종 위험도: ${(result.final_risk * 100).toInt()}%")
            }

            else -> {
                Text("분석할 이미지를 선택하세요.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("이미지 불러오기")
                }
            }
        }

        if (serverResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("다른 이미지 분석하기")
            }
        }
    }
}

@Composable
fun AnalyzedImageWithServerBoxes(
    bitmap: Bitmap,
    boxes: List<org.opencv.core.Rect>,
    modifier: Modifier = Modifier
) {
    val originalWidth = bitmap.width.toFloat()
    val originalHeight = bitmap.height.toFloat()

    Box(
        modifier = modifier.aspectRatio(originalWidth / originalHeight)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            val scaleX = size.width / originalWidth
            val scaleY = size.height / originalHeight

            boxes.forEach { rect ->
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(rect.x * scaleX, rect.y * scaleY),
                    size = Size(rect.width * scaleX, rect.height * scaleY),
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}
