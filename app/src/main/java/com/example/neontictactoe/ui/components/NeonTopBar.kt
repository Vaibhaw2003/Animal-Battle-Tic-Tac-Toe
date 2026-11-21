package com.example.neontictactoe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.neontictactoe.ui.theme.LocalNeonColors
import com.example.neontictactoe.ui.theme.withNeonGlow

@Composable
fun NeonTopBar(
  title: String,
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  actions: @Composable RowScope.() -> Unit = {},
) {
  val neon = LocalNeonColors.current
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f),
    ) {
      if (onBackClick != null) {
        NeonIconButton(
          icon = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = stringResource(R.string.cd_back),
          onClick = onBackClick,
          testTag = "back_button",
        )
        Spacer(modifier = Modifier.width(12.dp))
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.withNeonGlow(neon.primaryCyan, 12f),
        color = neon.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      content = actions,
    )
  }
}
