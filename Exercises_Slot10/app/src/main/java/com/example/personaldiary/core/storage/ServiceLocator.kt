    package com.example.personaldiary.core.storage

    import android.content.Context
    import android.util.Log
    import com.example.personaldiary.core.prefs.SettingsManager
    import com.example.personaldiary.data.local.DiaryDatabase
    import com.example.personaldiary.data.repo.NoteRepository

    object ServiceLocator {
        private const val TAG = "ServiceLocator"
        @Volatile private var repoInstance: NoteRepository? = null

        fun db(context: Context) = DiaryDatabase.get(context)

        fun notesRepo(context: Context): NoteRepository {
            return repoInstance ?: synchronized(this) {
                repoInstance ?: NoteRepository(db(context).noteDao()).also {
                    repoInstance = it
                    Log.d(TAG, "Created NEW NoteRepository instance")
                }
            }
        }

        fun settings(context: Context) = SettingsManager(context.applicationContext)

        /** Clear cached repository instance - gọi khi cần refresh sau backup/restore */
        fun clearRepoCache() {
            synchronized(this) {
                Log.d(TAG, "Clearing repository cache")
                repoInstance = null
            }
        }
    }