package city.zouitel.jetnote

import android.app.Application
import android.content.Context
import androidx.startup.AppInitializer
import city.zouitel.audios.di.exoPlayerKoinModule
import city.zouitel.database.di.databaseKoinModule
import city.zouitel.datastore.datastoreKoinModule
import city.zouitel.domain.di.useCasesKoinModule
import city.zouitel.init.WorkManagerInitializer
import city.zouitel.links.di.linksKoinModule
import city.zouitel.note.di.noteKoinModule
import city.zouitel.notifications.di.notificationKoinModule
import city.zouitel.quicknote.di.quickNoteKoinModule
import city.zouitel.recoder.di.recorderKoinModule
import city.zouitel.reminder.di.reminderKoinModule
import city.zouitel.repository.di.repositoryKoinModule
import city.zouitel.systemDesign.di.datastoreVMKoinModule
import city.zouitel.tags.di.tagsKoinModule
import city.zouitel.tasks.di.tasksKoinModule
import city.zouitel.widget.di.widgetKoinModule
import com.karacca.beetle.Beetle
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class NoteApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@NoteApplication)
//            workManagerFactory()
            modules(
                databaseKoinModule,
                datastoreKoinModule,
                repositoryKoinModule,
//                useCasesKoinModule,
//                initializerKoinModule,
                exoPlayerKoinModule,
                linksKoinModule,
                noteKoinModule,
                quickNoteKoinModule,
                recorderKoinModule,
                reminderKoinModule,
                tagsKoinModule,
                tasksKoinModule,
                widgetKoinModule,
                notificationKoinModule,
                datastoreVMKoinModule
            )
        }

        /**
         * Global Exception Handler.
         */
//        GlobalExceptionHandler.initialize(this, NoteActivity::class.java)

        /**
         *
         */
        Beetle.configure {
            enableAssignees = true
            enableLabels = true
        }
        Beetle.init(this, "City-Zouitel", "JetNote")

        /**
         * Work Manager Initializer.
         */
        AppInitializer.getInstance(this).initializeComponent(WorkManagerInitializer::class.java)
    }
}