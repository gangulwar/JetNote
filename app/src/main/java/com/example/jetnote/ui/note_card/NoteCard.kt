package com.example.jetnote.ui.note_card

import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetnote.cons.*
import com.example.jetnote.ui.navigation_drawer.Screens.HOME_SCREEN
import com.example.jetnote.ui.navigation_drawer.Screens.TRASH_SCREEN
import com.example.local.db.entities.Entity
import com.example.jetnote.fp.codeUrl
import com.example.jetnote.fp.findUrlLink
import com.example.jetnote.icons.*
import com.example.jetnote.ui.ImageDisplayed
import com.example.jetnote.ui.add_and_edit.UrlCard
import com.example.jetnote.ui.media_player_screen.NoteMediaPlayer
import com.example.jetnote.ui.navigation_drawer.Screens
import com.example.jetnote.ui.settings_screen.makeSound
import com.example.jetnote.vm.*
import com.example.local.db.entities.note.Note
import com.example.local.db.entities.note_and_todo.NoteAndTodo
import com.example.local.db.entities.todo.Todo
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import me.saket.swipe.rememberSwipeableActionsState
import java.io.File
import java.util.*

@Composable
fun NoteCard(
    forScreen: Screens,
    entity: Entity,
    navController: NavController,
    selectionState: MutableState<Boolean>?,
    selectedNotes: SnapshotStateList<Note>?,
    onSwipeNote: (Entity) -> Unit
) {
    val swipeState = rememberSwipeableActionsState()

    val ctx = LocalContext.current

    val noteDataStore = com.example.datastore.DataStore(ctx)
    // the true value is 'list' layout and false is 'grid'.
    val currentLayout = noteDataStore.getLayout.collectAsState(false).value

    val action = SwipeAction(
        onSwipe = {
            onSwipeNote.invoke(entity)
        },
        icon = {},
        background = Color.Transparent
    )

    if (currentLayout) {
        SwipeableActionsBox(
            modifier = Modifier,
            backgroundUntilSwipeThreshold = Color.Transparent,
            endActions = listOf(action),
            swipeThreshold = 100.dp,
            state = swipeState
        ) {
            Card(
                entity = entity,
                navController = navController,
                forScreens = forScreen,
                selectionState = selectionState,
                selectedNotes = selectedNotes
            )
        }
    } else {
        Card(
            entity = entity,
            navController = navController,
            forScreens = forScreen,
            selectionState = selectionState,
            selectedNotes = selectedNotes
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun Card(
    todoVM: TodoVM = hiltViewModel(),
    noteAndTodoVM: NoteAndTodoVM = hiltViewModel(),
    noteVM: NoteVM = hiltViewModel(),
    entity: Entity,
    navController: NavController,
    forScreens: Screens,
    selectionState: MutableState<Boolean>?,
    selectedNotes: SnapshotStateList<Note>?
) {
    val ctx = LocalContext.current
    val thereIsSoundEffect = com.example.datastore.DataStore(ctx).thereIsSoundEffect.collectAsState(false)

    val note = entity.note
    val labels = entity.labels
    val internalPath = ctx.filesDir.path

    val observeTodoList = remember(todoVM, todoVM::getAllTodoList).collectAsState()
    val observeNoteAndTodo =
        remember(noteAndTodoVM, noteAndTodoVM::getAllNotesAndTodo).collectAsState()

    val mediaPath = ctx.filesDir.path + "/$AUDIO_FILE/" + note.uid + "." + MP3
    val imagePath = "$internalPath/$IMAGE_FILE/${note.uid}.$JPEG"

    var todoListState by remember { mutableStateOf(false) }
    val media = remember { mutableStateOf<Uri?>(File(imagePath).toUri()) }

    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .combinedClickable(
                onLongClick = {
                    // To make vibration.
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    selectionState?.value = true
                    selectedNotes?.add(note)
                }
            ) {
                Unit.makeSound.invoke(ctx, KEY_CLICK, thereIsSoundEffect.value)

                if (forScreens == HOME_SCREEN && !selectionState?.value!!) {
                    navController.navigate(
                        route = EDIT_ROUTE + "/" +
                                note.uid + "/" +
                                codeUrl.invoke(note.title) + "/" +
                                codeUrl.invoke(note.description) + "/" +
                                note.color + "/" +
                                note.textColor + "/" +
                                note.priority + "/" +
                                note.audioDuration + "/" +
                                note.reminding
                    )
                } else {
                    when {
                        !selectedNotes?.contains(note)!! -> selectedNotes.add(note)
                        else -> selectedNotes.remove(note)
                    }
                }
                selectedNotes?.ifEmpty { selectionState?.value = false }
            }
            .drawBehind {
                if (note.priority.equals(NON, true)) {
                    normalNotePath(note)
                } else {
                    clipNotePath(note)
                }
            },
        shape = AbsoluteRoundedCornerShape(15.dp),
        border =
            if(selectedNotes?.contains(note) == true) BorderStroke(3.dp, Color.Cyan) else BorderStroke(0.dp, Color.Transparent) ,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)

    ) {

        // display the image.
        when (forScreens) {
            HOME_SCREEN, TRASH_SCREEN -> {
                ImageDisplayed(media = noteVM::imageDecoder.invoke(ctx, note.uid))
            }
            else -> { // Timber.tag(TAG).d("")
            }
        }

        Text(
            text = note.title ?: "",
            fontSize = 19.sp,
            color = Color(note.textColor),
            modifier = Modifier.padding(3.dp)
        )
        Text(
            text = note.description ?: "",
            fontSize = 15.sp,
            color = Color(note.textColor),
            modifier = Modifier.padding(start = 3.dp, end = 3.dp, bottom = 7.dp)
        )

        //media display.
                if (
                    File(mediaPath).exists()
                ) {
                    NoteMediaPlayer(localMediaUid = note.uid)
                }

        // labels.
        LazyRow {
            items(items = labels) { label ->
                AssistChip(
                    modifier = Modifier.alpha(.7f),
                    border = AssistChipDefaults.assistChipBorder(borderColor = Color.Transparent),
                    onClick = { },
                    label = {
                        label.label?.let { Text(it, fontSize = 11.sp) }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = CIRCLE_ICON_18),
                            contentDescription = null,
                            tint = Color(label.color),
                            modifier = Modifier.size(10.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = Color(note.textColor)
                    ),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
        }

        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (forScreens == TRASH_SCREEN) {
                IconButton(onClick = {
                    noteVM.updateNote(
                        Note(
                            title = note.title,
                            description = note.description,
                            priority = note.priority,
                            uid = note.uid,
                            trashed = 0,
                            color = note.color,
                            textColor = note.textColor
                        )
                    )
                }) {
                    Icon(
                        painterResource(id = RESET_ICON), null,
                        tint = Color(note.textColor)
                    )
                }
            }

            //
            if (forScreens==HOME_SCREEN && note.reminding != 0L) {
                note.reminding.let {
                    kotlin.runCatching {
                        ElevatedAssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    DateFormat.format("yyyy-MM-dd HH:mm", Date(it)).toString(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textDecoration = if (it < Calendar.getInstance().time.time) {
                                        TextDecoration.LineThrough
                                    } else {
                                        TextDecoration.None
                                    }
                                )
                            },
                            leadingIcon = {
                                if (it >= Calendar.getInstance().time.time) {
                                    Icon(painterResource(CLOCK_ICON), null)
                                }
                            }
                        )
                    }
                }
            }
        }
        if (
            observeTodoList.value.any {
                observeNoteAndTodo.value.contains(
                    NoteAndTodo(note.uid, it.id)
                )
            }
        ) {
            Icon(
                painterResource(
                    if (todoListState) ANGLE_UP_ICON else ANGLE_DOWN_ICON
                ),
                null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        todoListState = !todoListState
                    },
                tint = Color(note.textColor)
            )
        }

        findUrlLink(note.description)?.let{
            UrlCard(desc = it, true)
        }

        AnimatedVisibility(visible = todoListState, modifier = Modifier.height(100.dp)) {
            LazyColumn {
                item {
                    observeTodoList.value.filter {
                        observeNoteAndTodo.value.contains(
                            NoteAndTodo(note.uid, it.id)
                        )
                    }.forEach { todo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = todo.isDone, onClick = {
                                todoVM.updateTotoItem(
                                    Todo(
                                        id = todo.id,
                                        item = todo.item,
                                        isDone = !todo.isDone
                                    )
                                )
                            },
                                colors = RadioButtonDefaults.colors(
                                selectedColor = Color.Gray,
                                unselectedColor = Color(note.textColor)
                            ),
                                modifier = Modifier
                                    .padding(5.dp)
                                    .size(14.dp)
                            )
//                            Checkbox(
//                                checked = todo.isDone,
//                                onCheckedChange = {
//                                    todoVM.updateTotoItem(
//                                        Todo(
//                                            id = todo.id,
//                                            item = todo.item,
//                                            isDone = !todo.isDone
//                                        )
//                                    )
//                                },
//                                colors = CheckboxDefaults.colors(
//                                    checkedColor = Color.Gray,
//                                    uncheckedColor = Color(note.textColor)
//                                ),
//                                modifier = Modifier
//                                    .padding(5.dp)
//                                    .size(14.dp)
//                            )
                            todo.item?.let { item ->
                                Text(
                                    text = item,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp,
                                    style = TextStyle(
                                        textDecoration = if (todo.isDone) {
                                            TextDecoration.LineThrough
                                        } else {
                                            TextDecoration.None
                                        },
                                        color = if (todo.isDone) Color.Gray else Color(note.textColor)
                                    ),
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

