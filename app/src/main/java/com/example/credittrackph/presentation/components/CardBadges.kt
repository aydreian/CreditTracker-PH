package com.example.credittrackph.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.credittrackph.data.model.Bank
import com.example.credittrackph.data.model.CardType

/**
 * Renders authentic branded visual logos for Philippine Banks.
 */
@Composable
fun BankLogo(bank: Bank, modifier: Modifier = Modifier) {
    when (bank) {
        Bank.BDO -> {
            // Iconic BDO blue badge with yellow accent
            Row(
                modifier = modifier
                    .background(Color(0xFF003366), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(14.dp)
                        .background(Color(0xFFFFD100), RoundedCornerShape(2.dp))
                )
                Text(
                    "BDO",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
        Bank.BPI -> {
            // BPI crimson badge with shield accent
            Row(
                modifier = modifier
                    .background(Color(0xFFB30838), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height * 0.3f)
                        lineTo(size.width * 0.8f, size.height)
                        lineTo(size.width * 0.2f, size.height)
                        lineTo(0f, size.height * 0.3f)
                        close()
                    }
                    drawPath(path, Color(0xFFFFCC00))
                }
                Text(
                    "BPI",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
        Bank.UNIONBANK -> {
            // UnionBank vibrant orange badge
            Row(
                modifier = modifier
                    .background(Color(0xFFFF5900), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFFF5900), CircleShape)
                    )
                }
                Text(
                    "UnionBank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Bank.METROBANK -> {
            // Metrobank deep blue with diamond logo
            Row(
                modifier = modifier
                    .background(Color(0xFF002D72), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height / 2f)
                        lineTo(size.width / 2f, size.height)
                        lineTo(0f, size.height / 2f)
                        close()
                    }
                    drawPath(path, Color.White)
                }
                Text(
                    "Metrobank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Bank.RCBC -> {
            // RCBC blue badge with yellow flame
            Row(
                modifier = modifier
                    .background(Color(0xFF003882), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFFFCC00), CircleShape)
                )
                Text(
                    "RCBC",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
        Bank.SECURITY_BANK -> {
            // Security Bank green badge
            Row(
                modifier = modifier
                    .background(Color(0xFF007A33), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp, 12.dp)
                        .background(Color(0xFF00A3E0), RoundedCornerShape(2.dp))
                )
                Text(
                    "Security Bank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
        Bank.CITIBANK -> {
            // Citibank with red arc
            Row(
                modifier = modifier
                    .background(Color(0xFF003B70), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "citi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        Bank.HSBC -> {
            // HSBC red/white bowtie logo
            Row(
                modifier = modifier
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    // Draw red triangles
                    val red = Color(0xFFDB0011)
                    drawRect(red, size = size / 2f)
                }
                Text(
                    "HSBC",
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Bank.EASTWEST -> {
            Row(
                modifier = modifier
                    .background(Color(0xFF6C207E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "eastwest",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Bank.CHINABANK -> {
            Row(
                modifier = modifier
                    .background(Color(0xFFB81C22), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "CHINABANK",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
        else -> {
            // Styled generic badge with shortCode
            Box(
                modifier = modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    bank.shortCode,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Renders authentic Card Network Logos (Mastercard, Visa, JCB, AMEX).
 */
@Composable
fun CardNetworkLogo(cardType: CardType, modifier: Modifier = Modifier) {
    when (cardType) {
        CardType.MASTERCARD -> {
            // The iconic overlapping red and amber circles
            Row(
                modifier = modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFEB001B), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(x = (-8).dp)
                        .size(18.dp)
                        .background(Color(0xFFF79E1B).copy(alpha = 0.9f), CircleShape)
                )
            }
        }
        CardType.VISA -> {
            // Iconic stylized bold italic VISA emblem
            Box(
                modifier = modifier
                    .background(Color(0xFF1A1F71), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "VISA",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        CardType.JCB -> {
            // Iconic tricolor pill logo
            Row(
                modifier = modifier
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(Modifier.size(6.dp, 12.dp).background(Color(0xFF00367E), RoundedCornerShape(2.dp)))
                Box(Modifier.size(6.dp, 12.dp).background(Color(0xFFD81A28), RoundedCornerShape(2.dp)))
                Box(Modifier.size(6.dp, 12.dp).background(Color(0xFF00823B), RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(2.dp))
                Text("JCB", color = Color(0xFF00367E), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
        CardType.AMEX -> {
            // American Express classic blue box
            Box(
                modifier = modifier
                    .background(Color(0xFF006FCF), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    "AMEX",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
        CardType.OTHER -> {
            Box(
                modifier = modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("CARD", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 10.sp)
            }
        }
    }
}
