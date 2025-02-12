package com.flipperdevices.archive.search.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.flipperdevices.archive.search.R
import com.flipperdevices.archive.search.viewmodel.SearchViewModel
import com.flipperdevices.core.ui.R as DesignSystem
import com.flipperdevices.core.ui.composable.LocalRouter
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ComposableSearchBar(searchViewModel: SearchViewModel) {
    val systemUiController = rememberSystemUiController()
    val appBarColor = colorResource(DesignSystem.color.background)

    SideEffect {
        systemUiController.setStatusBarColor(appBarColor)
    }

    val router = LocalRouter.current
    var text by remember { mutableStateOf("") }
    key(text) {
        searchViewModel.onChangeText(text)
    }

    Row(
        modifier = Modifier.background(appBarColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ComposableSearchBarBack { router.exit() }
        ComposableSearchTextField(
            modifier = Modifier
                .weight(weight = 1f)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            text
        ) {
            text = it
        }
        Icon(
            modifier = Modifier
                .padding(end = 20.dp, top = 14.dp, bottom = 14.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = { text = "" }
                ),
            painter = painterResource(R.drawable.ic_clear),
            contentDescription = null
        )
    }
}

@Composable
fun ComposableSearchBarBack(onBack: () -> Unit) {
    Icon(
        modifier = Modifier
            .padding(start = 24.dp, top = 14.dp, bottom = 14.dp)
            .size(size = 24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onBack
            ),
        painter = painterResource(R.drawable.ic_back_arrow),
        contentDescription = null,
        tint = colorResource(DesignSystem.color.black_100)
    )
}
