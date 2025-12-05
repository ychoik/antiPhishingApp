package com.example.antiphishingapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiphishingapp.R
import com.example.antiphishingapp.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CallLog(
    val timestamp: String,
    val text: String
)

data class CallRecord(
    val phoneNumber: String,
    val callTime: String,
    val logs: List<CallLog>,
    val isRisky: Boolean
)

private val phishingKeywords = listOf(
    "안전계좌","보안계좌","현금 전달","대포통장","계좌 이체","송금 요청",
    "개인정보 확인","비밀번호 입력","인증번호 전송","송금","이체","입금","구속","형사처벌","압류","고소","체포",
    "영장","계좌번호","비밀번호","인증번호","OTP","보안카드","검찰","검찰청","경찰","경찰청","금융감독원","금감원",
    "법원","국세청","관세청","우체국","은행","카드사","통신사","긴급","즉시","24시간 이내","오늘 중","피의자",
    "명의 도용","개인정보 유출","사건 번호","출석요구서","소환장","전화 주세요","연락 바랍니다","클릭","링크",
    "앱 설치","프로그램 설치","대출","저금리","신용","한도","승인","연체","채무","미납","미수","정지","해지",
    "택배","배송","상품권","쿠폰","당첨","경품","무료","계좌","벌금","확인요망","확인하세요","조회","인증"
)

private val rawCallData = listOf(
    Triple("010-1234-5678", "PM 10:30", listOf(
        CallLog("00:00", "즉시 송금 요망. 확인 부탁드립니다."),
        CallLog("02:10", "계좌가 정지된 것으로 보여요.")
    )),
    Triple("010-8765-4321", "PM 09:30", listOf(
        CallLog("01:30", "우체부 등기가 도착했는데 혹시 지금 댁에 계실까요?"),
        CallLog("01:43", "특수범죄수사부 3팀 경감...")
    )),
    Triple("010-1111-2222", "AM 08:12", listOf(
        CallLog("23:14", "긴급한 건이라서요.")
    )),
    Triple("010-5555-4444", "PM 04:55", listOf(
        CallLog("04:20", "의심 계좌로 확인되어 연락드렸는데 시간 괜찮으실까요?")
    ))
)

private val sampleCallRecords = rawCallData.map { (phone, time, logs) ->
    val fullContent = logs.joinToString(" ") { it.text }
    CallRecord(
        phoneNumber = phone,
        callTime = time,
        logs = logs,
        isRisky = phishingKeywords.any { fullContent.contains(it) }
    )
}

private fun parseTime(timeString: String): LocalTime {
    return try {
        val formatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.US)
        LocalTime.parse(timeString, formatter)
    } catch (e: Exception) {
        LocalTime.MIN
    }
}

@Composable
fun CallListScreen() {
    var selectedOption by remember { mutableStateOf("최신순") }
    var displayedRecords by remember { mutableStateOf(sampleCallRecords) }

    LaunchedEffect(selectedOption) {
        displayedRecords = when (selectedOption) {
            "최신순" -> sampleCallRecords.sortedByDescending { parseTime(it.callTime) }
            "번호순" -> sampleCallRecords.sortedBy { it.phoneNumber }
            "위험도순" -> sampleCallRecords // 정렬 미구현
            else -> sampleCallRecords
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary100)
            .padding(start = 24.dp, end = 27.dp)
    ) {
        Spacer(modifier = Modifier.height(67.dp))
        SearchBar()
        Spacer(modifier = Modifier.height(20.dp))
        FilterBar(
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it }
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayedRecords) { record ->
                CallCard(record = record)
            }
        }
    }
}

@Composable
private fun SearchBar() {
    var text by remember { mutableStateOf("") }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        singleLine = true,
        textStyle = AppTypography.bodyLarge.copy(color = Primary800),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .background(
                        color = Primary200,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mag),
                    contentDescription = "Search Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Primary800)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "원하는 내역을 검색하세요.",
                            style = AppTypography.bodyLarge,
                            color = Primary800
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun FilterBar(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val filterTextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = R.drawable.filter),
            contentDescription = "Filter Icon",
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(Grayscale900)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "필터",
            style = filterTextStyle,
            color = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.weight(1f))

        SortDropdownMenu(
            selectedOption = selectedOption,
            onOptionSelected = onOptionSelected
        )

        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
private fun SortDropdownMenu(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val sortOptions = listOf("최신순", "위험도순", "번호순")

    val textStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )

    Box {
        Row(
            modifier = Modifier
                .width(80.dp)
                .clickable { isExpanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = selectedOption,
                style = textStyle,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.poly),
                contentDescription = "Sort order",
                modifier = Modifier.size(width = 9.dp, height = 6.dp),
                colorFilter = ColorFilter.tint(Color(0xFFD9D9D9))
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .width(92.dp)
                .height(94.dp)
                .background(
                    color = Primary100,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                sortOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val textColor = if (option == selectedOption) {
                            Color(0xFF757575)
                        } else {
                            Color(0xFF9B9B9B)
                        }

                        Text(
                            text = option,
                            style = textStyle,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        val iconRes = if (option == selectedOption) {
                            R.drawable.checked01
                        } else {
                            R.drawable.unchecked01
                        }
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = "$option selection state",
                            modifier = Modifier
                                .size(10.dp)
                                .clickable {
                                    onOptionSelected(option)
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun CallCard(record: CallRecord) {
    val cardTextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Grayscale100,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.pic02),
            contentDescription = "Call Icon",
            modifier = Modifier.size(32.dp),
            colorFilter = ColorFilter.tint(Primary900)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.phoneNumber,
                    style = cardTextStyle,
                    color = Grayscale900
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = record.callTime,
                    style = cardTextStyle,
                    color = Grayscale900,
                    textAlign = TextAlign.Right
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                record.logs.forEach { log ->
                    Row {
                        Text(
                            text = log.timestamp,
                            style = cardTextStyle.copy(color = Grayscale900)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        HighlightedText(
                            text = log.text,
                            isRisky = record.isRisky,
                            style = cardTextStyle.copy(color = Grayscale900)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    isRisky: Boolean,
    style: TextStyle,
) {
    if (!isRisky) {
        Text(text = text, style = style)
        return
    }

    val annotatedString = buildAnnotatedString {
        val regex = Regex(phishingKeywords.joinToString("|"))
        val matches = regex.findAll(text)
        var lastIndex = 0

        for (match in matches) {
            append(text.substring(lastIndex, match.range.first))
            withStyle(style = SpanStyle(color = Primary900)) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    Text(text = annotatedString, style = style)
}

@Preview(showBackground = true)
@Composable
fun CallListScreenPreview() {
    AntiPhishingAppTheme {
        CallListScreen()
    }
}