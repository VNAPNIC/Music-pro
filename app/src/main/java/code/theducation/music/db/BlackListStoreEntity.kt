package code.theducation.music.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class BlackListStoreEntity(
    @PrimaryKey
    val path: String
)
