package com.fdtracker.feature.fdlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.ui.component.FdCard

@Composable
fun FdListView(
    fds: List<FixedDeposit>,
    onFdClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(fds, key = { it.fdAccountNumber }) { fd ->
            FdCard(fd = fd, onClick = { onFdClick(fd.fdAccountNumber) })
        }
    }
}
