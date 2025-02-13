package city.zouitel.note.ui.edit_screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import city.zouitel.audios.ui.AudioScreenModel
import city.zouitel.audios.ui.NormalMediaPlayer
import city.zouitel.links.model.NoteAndLink
import city.zouitel.links.ui.CacheLinks
import city.zouitel.links.ui.LinkCard
import city.zouitel.links.ui.LinkScreenModel
import city.zouitel.links.ui.NoteAndLinkScreenModel
import city.zouitel.logic.decodeUrl
import city.zouitel.note.ui.DataScreenModel
import city.zouitel.note.model.Data
import city.zouitel.note.ui.bottom_bar.AddEditBottomBar
import city.zouitel.notifications.viewmodel.NotificationScreenModel
import city.zouitel.reminder.ui.RemindingNote
import city.zouitel.systemDesign.CommonTextField
import city.zouitel.systemDesign.Cons.REC_DIR
import city.zouitel.systemDesign.Cons.IMG_DIR
import city.zouitel.systemDesign.Cons.JPEG
import city.zouitel.systemDesign.Cons.KEY_STANDARD
import city.zouitel.systemDesign.Cons.MP3
import city.zouitel.systemDesign.Cons.NON
import city.zouitel.systemDesign.DataStoreScreenModel
import city.zouitel.systemDesign.Icons
import city.zouitel.systemDesign.Icons.CIRCLE_ICON_18
import city.zouitel.systemDesign.Icons.EDIT_ICON
import city.zouitel.systemDesign.ImageDisplayed
import city.zouitel.systemDesign.SoundEffect
import city.zouitel.systemDesign.findUrlLink
import city.zouitel.tags.model.NoteAndTag
import city.zouitel.tags.viewmodel.NoteAndTagScreenModel
import city.zouitel.tags.viewmodel.TagScreenModel
import city.zouitel.tasks.model.NoteAndTask
import city.zouitel.tasks.model.Task
import city.zouitel.tasks.viewmodel.NoteAndTaskScreenModel
import city.zouitel.tasks.viewmodel.TaskScreenModel
import com.google.accompanist.flowlayout.FlowRow
import org.koin.core.component.KoinComponent
import java.io.File
import java.util.Date

data class EditScreen(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val color: Int = 0,
    val textColor: Int = 0,
    val priority: String = NON,
    val audioDuration: Int = 0,
    val reminding: Long = 0
): Screen, KoinComponent {

    @OptIn(ExperimentalFoundationApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    override fun Content() {
//        val navController: NavController = rememberNavController()
//        val exoViewModule: AudioScreenModel by inject()
//        val tagViewModel: TagScreenModel by inject()
//        val noteAndTodoVM: NoteAndTaskScreenModel by inject()
//        val dataStoreVM: DataStoreScreenModel by inject()
//        val linkVM: LinkScreenModel by inject()
//        val noteAndLinkVM: NoteAndLinkScreenModel by inject()

        val titleState = rememberTextFieldState(initialText = decodeUrl.invoke(title).orEmpty())
        val descriptionState = rememberTextFieldState(initialText = decodeUrl.invoke(description).orEmpty())
        var focusState by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val keyboardManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val internalPath = context.filesDir.path

        val focusRequester by lazy { FocusRequester() }
        val sound by lazy { SoundEffect() }
        val mediaFile by lazy {
            arrayOf(
                internalPath,
                '/',
                REC_DIR,
                '/',
                id,
                '.',
                MP3
            ).joinToString("")
        }
        val imageFile by lazy {
            arrayOf(
                internalPath,
                '/',
                IMG_DIR,
                '/',
                id,
                '.',
                JPEG
            ).joinToString("")
        }
        val dateState by lazy { mutableStateOf(Calendar.getInstance().time) }
        val bitImg by lazy { BitmapFactory.decodeFile(imageFile) }

        val notificationModel = getScreenModel<NotificationScreenModel>()
        val dataModel = getScreenModel<DataScreenModel>()
        val tagModel = getScreenModel<TagScreenModel>()
        val noteAndTagModel = getScreenModel<NoteAndTagScreenModel>()
        val taskModel = getScreenModel<TaskScreenModel>()
        val noteAndTodoModel = getScreenModel<NoteAndTaskScreenModel>()
        val audioModel = getScreenModel<AudioScreenModel>()
        val linkModel = getScreenModel<LinkScreenModel>()
        val noteAndLinkModel = getScreenModel<NoteAndLinkScreenModel>()
        val dataStoreModel = getScreenModel<DataStoreScreenModel>()

        val backgroundColorState = rememberSaveable { mutableIntStateOf(color) }
        val textColorState = rememberSaveable { mutableIntStateOf(textColor) }
        val priorityState = remember { mutableStateOf(priority) }
        val remindingDialogState = remember { mutableStateOf(false) }
        val recordDialogState = remember { mutableStateOf(false) }
        val photoState = remember { mutableStateOf<Bitmap?>(bitImg) }
        val remindingValue = remember { mutableLongStateOf(reminding) }
        val audioDurationState = remember { mutableIntStateOf(0) }
        val isTitleFieldFocused = remember { mutableStateOf(false) }
        val isDescriptionFieldFocused = remember { mutableStateOf(false) }

        val thereIsSoundEffect by remember(dataStoreModel, dataStoreModel::getSound).collectAsState()
        val observeNotesAndLabels by remember(
            noteAndTagModel,
            noteAndTagModel::getAllNotesAndTags
        ).collectAsState()
        val observeLabels by remember(tagModel, tagModel::getAllLTags).collectAsState()
        val observerLinks by remember(linkModel, linkModel::getAllLinks).collectAsState()
        val observerNoteAndLink by remember(
            noteAndLinkModel,
            noteAndLinkModel::getAllNotesAndLinks
        ).collectAsState()
        val observeTodoList by remember(
            taskModel,
            taskModel::getAllTaskList
        ).collectAsState()
        val observeNoteAndTodo by remember(
            noteAndTodoModel,
            noteAndTodoModel::getAllNotesAndTask
        ).collectAsState()
        var imageUriState by remember { mutableStateOf<Uri?>(File(imageFile).toUri()) }
        val img by rememberSaveable { mutableStateOf(photoState) }

        val chooseImageLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                imageUriState = it
                dataModel::decodeBitmapImage.invoke(img, photoState, it!!, context)
                img.value = photoState.value
                dataModel::saveImageLocally.invoke(
                    img.value, "$internalPath/$IMG_DIR", "$id.$JPEG"
                )
            }

        Scaffold(
            modifier = Modifier
                .navigationBarsPadding(),
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.outlineVariant,
                    contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.outlineVariant),
                    onClick = {
                        sound.makeSound.invoke(context, KEY_STANDARD, thereIsSoundEffect)
                        dataModel.editData(
                            Data(
                                title = if (titleState.text.isBlank()) null else titleState.text.toString(),
                                description = if (descriptionState.text.isBlank()) null else descriptionState.text.toString(),
                                priority = priorityState.value,
                                uid = id,
                                audioDuration = audioDuration,
                                reminding = remindingValue.longValue,
                                date = dateState.value.toString(),
                                trashed = 0,
                                color = backgroundColorState.intValue,
                                textColor = textColorState.intValue,
                            )
                        )
                        navigator?.pop()
                    }) {
                    Icon(
                        painter = painterResource(id = EDIT_ICON),
                        null
                    )
                }
            },
            bottomBar = {
                AddEditBottomBar(
                    dataStoreModel = dataStoreModel,
                    note = Data(uid = id),
//                    navController = navController,
                    imageLaunch = chooseImageLauncher,
                    recordDialogState = recordDialogState,
                    remindingDialogState = remindingDialogState,
                    backgroundColorState = backgroundColorState,
                    textColorState = textColorState,
                    priorityColorState = priorityState,
                    remindingValue = remindingValue,
                    titleState = Pair(titleState, isTitleFieldFocused.value),
                    descriptionState = descriptionState,
                )
            }
        ) {
            // recording dialog visibility.
//            if (recordDialogState.value) {
//                RecorderScreen(id = id, dialogState = recordDialogState)
//            }

            // reminding dialog visibility.
            if (remindingDialogState.value) {
                RemindingNote(
                    dataStoreModel = dataStoreModel,
                    notificationModel = notificationModel,
                    dialogState = remindingDialogState,
                    remindingValue = remindingValue,
                    title = titleState.text.toString(),
                    message = descriptionState.text.toString(),
                    uid = id
                )
            }

            LazyColumn(
                Modifier
                    .background(Color(backgroundColorState.intValue))
                    .fillMaxSize()
            ) {

                // display the image.
                item {
                    ImageDisplayed(media = img.value?.asImageBitmap())
                }

                // The Title.
                item {
                    CommonTextField(
                        state = titleState,
                        modifier = Modifier
                            .padding(10.dp)
                            .height(30.dp)
                            .focusRequester(focusRequester)
                            .onFocusEvent {
                                isTitleFieldFocused.value = it.isFocused
                            },

                        placeholder = "Title",
                        textSize = 24,
                        textColor = textColorState.intValue,
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { keyboardManager.moveFocus(FocusDirection.Next) }
                        )
                    )
                }

                // The Description.
                item {
                    CommonTextField(
                        state = descriptionState,
                        modifier = Modifier
                            .padding(10.dp)
                            .height(60.dp)
                            .onFocusEvent {
                                isDescriptionFieldFocused.value = it.isFocused
                                focusState = it.isFocused
                            },
                        placeholder = "Note",
                        textSize = 19,
                        textColor = textColorState.intValue,
                        imeAction = ImeAction.None,
                        keyboardActions = KeyboardActions(
                            /*onDone = {
                                keyboardController?.hide()
                                keyboardManager.clearFocus()
                            }*/
                        )
                    )
                }

                // display the media player.
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    if (
                        File(mediaFile).exists() && !recordDialogState.value
                    ) {
                        NormalMediaPlayer(audioScreenModel = audioModel, localMediaUid = id)
                        audioDurationState.intValue =
                            audioModel.getMediaDuration(context, mediaFile).toInt()

                    }
                }

                // Link display.
                item {
                    findUrlLink(descriptionState.text.toString())?.let { url ->
                        CacheLinks(
                            linkScreenModel = linkModel,
                            noteAndLinkScreenModel = noteAndLinkModel,
                            noteId = id,
                            url = url
                        )
                    }
                    // for refresh this screen.
                    observerLinks.filter {
                        observerNoteAndLink.contains(
                            NoteAndLink(id, it.id)
                        )
                    }.forEach { _link ->
                        LinkCard(
                            linkScreenModel = linkModel,
                            noteAndLinkScreenModel = noteAndLinkModel,
                            noteUid = id,
                            swipeable = true,
                            link = _link
                        )
                    }
                }

                // display reminding chip.
                if (remindingValue.longValue != 0L) {
                    item {
                        remindingValue.longValue.let {
                            runCatching {
                                ElevatedAssistChip(
                                    modifier = Modifier.padding(start = 15.dp),
                                    onClick = {},
                                    label = {
                                        Text(
                                            DateFormat.format("yyyy-MM-dd HH:mm", Date(it))
                                                .toString(),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textDecoration = if (it < java.util.Calendar.getInstance().time.time) {
                                                TextDecoration.LineThrough
                                            } else {
                                                TextDecoration.None
                                            },
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    },
                                    leadingIcon = {
                                        if (it >= java.util.Calendar.getInstance().time.time) {
                                            Icon(
                                                painterResource(Icons.BELL_RING_ICON),
                                                null,
                                                tint = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        }
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color(.6f, .6f, .6f, .5f)
                                    ),
                                    elevation = AssistChipDefaults.assistChipElevation()
                                )
                            }
                        }
                    }
                }

                // display all added tagEntities.
                item {
                    FlowRow {
                        observeLabels.filter {
                            observeNotesAndLabels.contains(
                                NoteAndTag(id, it.id)
                            )
                        }.forEach {
                            AssistChip(
                                onClick = { },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = CIRCLE_ICON_18),
                                        contentDescription = null,
                                        tint = Color(it.color),
                                        modifier = Modifier.size(10.dp)
                                    )
                                },
                                label = {
                                    it.label?.let { it1 -> Text(it1) }
                                }
                            )
                        }
                    }
                }

                // display the todo list.
                item {
                    observeTodoList.filter {
                        observeNoteAndTodo.contains(
                            NoteAndTask(id, it.id)
                        )
                    }.forEach { todo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = todo.isDone,
                                onCheckedChange = {
                                    taskModel.updateTotoItem(
                                        Task(
                                            id = todo.id,
                                            item = todo.item,
                                            isDone = !todo.isDone
                                        )
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.Gray,
                                    uncheckedColor = Color(textColorState.intValue)
                                )
                            )

                            todo.item?.let { item ->
                                Text(
                                    text = item,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (todo.isDone) Color.Gray else Color(
                                            textColorState.intValue
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

                // void space.
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
        }
    }
}
