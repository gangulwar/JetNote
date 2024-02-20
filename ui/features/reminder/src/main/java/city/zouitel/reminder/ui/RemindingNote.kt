package city.zouitel.reminder.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import city.zouitel.notifications.viewmodel.NotificationVM
import city.zouitel.reminder.utils.Cons.SINGLE_DAY
import city.zouitel.systemDesign.AdaptingRow
import city.zouitel.systemDesign.Cons.KEY_CLICK
import city.zouitel.systemDesign.Cons.KEY_STANDARD
import city.zouitel.systemDesign.DataStoreVM
import city.zouitel.systemDesign.Icons.CALENDAR_ICON
import city.zouitel.systemDesign.Icons.CLOCK_ICON
import city.zouitel.systemDesign.Icons.RESET_ICON
import city.zouitel.systemDesign.MaterialColors
import city.zouitel.systemDesign.MaterialColors.Companion.SURFACE
import city.zouitel.systemDesign.SoundEffect
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint(
    "UnspecifiedImmutableFlag",
    "UnrememberedMutableState"
)
@Composable
fun RemindingNote(
    dataStoreVM: DataStoreVM = koinViewModel(),
    notificationVM: NotificationVM = koinViewModel(),
    dialogState: MutableState<Boolean>,
    title: String?,
    message: String?,
    uid: String?,
    remindingValue: MutableLongState?
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val soundEffect = remember(dataStoreVM, dataStoreVM::getSound).collectAsState()
    val sound = SoundEffect()

    val getMatColor = MaterialColors().getMaterialColor

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - SINGLE_DAY
            }
        }
    )

    val selectedDate = remember { mutableLongStateOf(dateState.selectedDateMillis!!) }
    val selectedTime = remember { mutableLongStateOf(0L) }

    val datePickerDialog = remember { mutableStateOf(false) }
    val timePickerDialog = remember { mutableStateOf(false) }

    var dateTime = selectedTime.longValue + selectedDate.longValue

    if(datePickerDialog.value) {
        DateLayout(selectedDate = selectedDate, dateState = dateState, dateDialog = datePickerDialog) {
            sound.makeSound(context, KEY_CLICK, soundEffect.value)
        }
    }

    if (timePickerDialog.value) {
        TimeLayout(selectedTime = selectedTime, timePickerDialog = timePickerDialog) {
            sound.makeSound(context, KEY_CLICK, soundEffect.value)
        }
    }

    val formatter = SimpleDateFormat("dd MM yyyy", Locale.ROOT)

    AlertDialog(
        onDismissRequest = {
            dialogState.value = false
        },
        title = {
            Row {
                AdaptingRow(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Add Reminding", fontSize = 25.sp)
                }
            }
        },
        text = {
            Column {
                OutlinedIconButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = {
                        sound.makeSound(context, KEY_CLICK, soundEffect.value)
                        datePickerDialog.value = true
                    }) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp)
                    ) {
                        Icon(
                            painterResource(CALENDAR_ICON), null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Today", fontSize = 17.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedIconButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = {
                        sound.makeSound(context, KEY_CLICK, soundEffect.value)
                        timePickerDialog.value = true
                    }) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp)
                    ) {
                        Icon(
                            painterResource(CLOCK_ICON), null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Pick Time", fontSize = 17.sp)
                    }
                }
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedIconButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            sound.makeSound(context, KEY_CLICK, soundEffect.value)
                            dateTime = 0L
                        }) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp)
                        ) {
                            Icon(
                                painterResource(RESET_ICON),null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Reset", fontSize = 17.sp)
                        }
                }
            }
               },
        confirmButton = {
            OutlinedIconButton(
                modifier = Modifier
                    .size(90.dp,35.dp),
                onClick = {
                    sound.makeSound(context, KEY_STANDARD, soundEffect.value)
                    runCatching {
                        notificationVM.scheduleNotification(
                            context = context,
                            dateTime = dateTime,
                            title = title,
                            message = message,
                            uid = uid
                        ) {
                            /**
                             * if true the work manager should be canceled.
                             */
                            dateTime == 0L
                        }
                    }.onSuccess {
                        remindingValue?.longValue = dateTime
                    }
                    dialogState.value = false
                }) {
                Text(text = "Save", fontSize = 16.sp)
            }
        },
        dismissButton = {
            OutlinedIconButton(
                modifier = Modifier
                    .size(90.dp,35.dp),
                onClick = {
                    sound.makeSound(context, KEY_CLICK, soundEffect.value)
                    dialogState.value = false
                }) {
                Text(text = "Cansel", fontSize = 16.sp)
            }
        },
        containerColor = getMatColor(SURFACE)
    )
}