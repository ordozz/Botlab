package com.example.tnotes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp), // M3 default is 12.dp for some components
    large = RoundedCornerShape(16.dp)  // M3 default is 16.dp or 28.dp for larger components
)
