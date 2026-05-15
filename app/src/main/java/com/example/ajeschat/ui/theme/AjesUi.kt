package com.example.ajeschat.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

val AjesCardShape = RoundedCornerShape(16.dp)
val AjesCardShapeMedium = RoundedCornerShape(14.dp)
val AjesInputShape = RoundedCornerShape(8.dp)

fun Modifier.ajesScreenBackground(): Modifier = this.background(AjesMintBg)

@Composable
fun ajesCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
fun ajesCardElevation() = CardDefaults.cardElevation(defaultElevation = 2.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ajesTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    scrolledContainerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = AjesOnGreen,
    navigationIconContentColor = AjesOnGreen,
    actionIconContentColor = AjesOnGreen
)

/** Main tabs — logo only, no green bar; blends with mint screen background. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ajesLogoTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = AjesMintBg,
    scrolledContainerColor = AjesMintBg,
    titleContentColor = AjesTextPrimary,
    navigationIconContentColor = AjesTextPrimary,
    actionIconContentColor = AjesTextPrimary
)

@Composable
fun ajesTextButtonColors() = ButtonDefaults.textButtonColors(
    contentColor = AjesTextPrimary,
    disabledContentColor = AjesTextSecondary
)

@Composable
fun ajesTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AjesTextPrimary,
    unfocusedTextColor = AjesTextPrimary,
    disabledTextColor = AjesTextPrimary,
    errorTextColor = AjesError,
    focusedLabelColor = AjesTextPrimary,
    unfocusedLabelColor = AjesTextPrimary,
    disabledLabelColor = AjesTextPrimary,
    focusedPlaceholderColor = AjesTextSecondary,
    unfocusedPlaceholderColor = AjesTextSecondary,
    focusedBorderColor = AjesGreen,
    unfocusedBorderColor = AjesBorder,
    disabledBorderColor = AjesBorder,
    cursorColor = AjesGreen,
    focusedTrailingIconColor = AjesTextPrimary,
    unfocusedTrailingIconColor = AjesTextPrimary,
    disabledTrailingIconColor = AjesTextSecondary
)

/** Side / bottom nav — light bar with high-contrast icons (AJES web style). */
@Composable
fun ajesNavigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AjesGreen,
    selectedTextColor = AjesTextPrimary,
    unselectedIconColor = AjesMutedGreen,
    unselectedTextColor = AjesTextSecondary,
    indicatorColor = AjesBorder,
    disabledIconColor = AjesTextSecondary.copy(alpha = 0.5f),
    disabledTextColor = AjesTextSecondary.copy(alpha = 0.5f)
)

@Composable
fun ajesNavigationRailItemColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = AjesGreen,
    selectedTextColor = AjesTextPrimary,
    unselectedIconColor = AjesMutedGreen,
    unselectedTextColor = AjesTextSecondary,
    indicatorColor = AjesBorder,
    disabledIconColor = AjesTextSecondary.copy(alpha = 0.5f),
    disabledTextColor = AjesTextSecondary.copy(alpha = 0.5f)
)

@Composable
fun Modifier.ajesCardBorder(): Modifier = border(
    width = 1.dp,
    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    shape = AjesCardShape
)

/** Mint welcome gradient — matches web `.welcome-card` / `.kpi-mint` */
@Composable
fun ajesMintGradient(): Brush = Brush.linearGradient(
    colors = listOf(AjesMintBg, AjesBorder)
)

@Composable
fun ajesSageGradient(): Brush = Brush.linearGradient(
    colors = listOf(AjesBorder, AjesSage)
)

@Composable
fun ajesGreenGradient(): Brush = Brush.linearGradient(
    colors = listOf(AjesSage, AjesGreenAccent)
)
