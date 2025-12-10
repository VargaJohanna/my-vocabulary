package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.ProvideAppBarTitle
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar
import kotlin.getValue

@Composable
fun DictionaryListScreen() {
    val viewModel: DictionaryListViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.fetchDictionaries()
    }

    val dictionaryList by viewModel.dictionaries.collectAsState()
    ProvideAppBarTitle({ Text(stringResource(R.string.dictionaries_toolbar)) })

    Scaffold(
        floatingActionButton = {
            AddDictionaryFAB()
        }
    ) {
        FlowColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            DictionaryList(dictionaryList)
        }

    }
}

@Composable
fun AddDictionaryFAB() {
    FloatingActionButton(onClick = {}) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

@Composable
fun FABMenu(modifier : Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

//    FloatingActionButtonMenu(
//        expanded = expanded,
//        button = {
//
//        }
//    ) {
//        this.FloatingActionButtonMenuItem()
//    }


}

@Composable
fun DictionaryItemView(dictionaryItem: Dictionary, modifier: Modifier = Modifier) {
    val padding = 8.dp
    Card(
        modifier
            .fillMaxWidth()
            .padding(padding),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(28.dp),
                text = dictionaryItem.dictionaryName,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            DictionaryOptionsButton(dictionaryItem)

        }
    }
}

@Preview (showBackground = true)
@Composable
fun DictionaryOptionsButton(dictonaryItem: Dictionary) {
    var expanded by remember { mutableStateOf(false) }
    Box{
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.dict_options_description),
                )
             }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }){
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dictionary_menu_start_quiz)) },
                onClick = { /* Do something... */ }
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text(stringResource(R.string.dictionary_menu_rename)) },
                onClick = { /* Do something... */ }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dictionary_menu_delete)) },
                onClick = { /* Do something... */ }
            )
        }
    }

}

@Composable
fun DictionaryList(list: List<Dictionary>) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(list) { item ->
            DictionaryItemView(item)
        }
    }
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) {
        FlowColumn(
            modifier = Modifier.fillMaxSize(),

            ) {
            DictionaryList(
                listOf(
                    Dictionary(
                        dictionaryId = 1L,
                        dictionaryName = "Test",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 2L,
                        dictionaryName = "Test2",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 3L,
                        dictionaryName = "Test3",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 4L,
                        dictionaryName = "Test4",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),
                    Dictionary(
                        dictionaryId = 5L,
                        dictionaryName = "Test5",
                        dictionaryCreated = Calendar.getInstance().time
                    ),

                    )
            )

        }
    }
}

