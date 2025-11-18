package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocabulary.myvocabulary.ui.home.ButtonCard

@Composable
fun DictionaryListScreen() {
    FlowColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ButtonCard( text = "Dictionary List")

    }
}

@Composable
fun ButtonCard(text: String) {
    Row(modifier = Modifier
        .padding(all = 32.dp)
        .fillMaxWidth()
    ) {
        Button(onClick = {} ,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text, modifier = Modifier.padding(32.dp), fontSize = 32.sp)
        }
    }
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    FlowColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ButtonCard( text = "Dictionary List")

    }
}