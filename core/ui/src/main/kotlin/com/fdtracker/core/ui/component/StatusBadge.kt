package com.fdtracker.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fdtracker.core.ui.theme.FdActive
import com.fdtracker.core.ui.theme.FdExpired
import com.fdtracker.core.ui.theme.FdGracePeriod
import com.fdtracker.core.ui.theme.FdMaturing
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class FdStatus { ACTIVE, MATURING_SOON, MATURED, GRACE_PERIOD, EXPIRED }

@Composable
fun StatusBadge(
    maturityDate: LocalDate,
    gracePeriodDays: Int = 7,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val daysToMaturity = ChronoUnit.DAYS.between(today, maturityDate)

    val (text, color) = when {
        daysToMaturity > 30 -> "Active" to FdActive
        daysToMaturity in 1..30 -> "Maturing Soon" to FdMaturing
        daysToMaturity == 0L -> "Matures Today" to FdMaturing
        daysToMaturity in (-gracePeriodDays.toLong())..(-1L) -> "Grace Period" to FdGracePeriod
        else -> "Matured" to FdExpired
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
