package com.innerken.aadenprinterx.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innerken.aadenprinterx.viewmodel.PrinterViewModel
import com.innerken.aadenprinterx.R

@Composable
fun IndexPage(
    printerViewModel: PrinterViewModel,
    onHomeClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onConfigClick: () -> Unit
) {
    val status by printerViewModel.status.collectAsState()
    val countText by printerViewModel.countText.collectAsState()

    val context = LocalContext.current
    val versionName = context.packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF5E5))
            .padding(32.dp)
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
        ) {

            Text(
                text = "Aaden Printer X",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "v$versionName",
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Status Icon"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Status/状态:",
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = status,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate, // 示例图标
                    contentDescription = "Count Icon"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Count/计数:",
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = countText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onConfigClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFB8C00),
                    contentColor = Color.White
                )) {
                Text("Configure/配置")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = onHomeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF26A69A),
                    contentColor = Color.White
                )) {
                Text("Home/主屏幕")
            }

        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_laucher),
                contentDescription = "Logo 1",
                modifier = Modifier
                    .size(84.dp)
                    .padding(end = 4.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                onDoubleClick()
                            }
                        )
                    }
            )
        }
    }
}
