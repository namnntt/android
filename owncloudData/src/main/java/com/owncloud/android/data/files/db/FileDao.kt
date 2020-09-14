/**
 * ownCloud Android client application
 *
 * @author Abel García de Prada
 * Copyright (C) 2020 ownCloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.data.files.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.owncloud.android.data.ProviderMeta

@Dao
abstract class FileDao {

    @Query(SELECT_FILE_WITH_ID)
    abstract fun getFileById(
        id: Long
    ): OCFileEntity?

    @Query(SELECT_FILE_FROM_OWNER_WITH_REMOTE_PATH)
    abstract fun getFileByOwnerAndRemotePath(
        owner: String,
        remotePath: String
    ): OCFileEntity?

    @Query(SELECT_FOLDER_CONTENT)
    abstract fun getFolderContent(
        folderId: Long
    ): List<OCFileEntity>

    @Query(SELECT_FOLDER_BY_MIMETYPE)
    abstract fun getFolderByMimeType(
        folderId: Long,
        mimeType: String
    ): List<OCFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(ocFileEntity: OCFileEntity): Long

    @Transaction
    open fun mergeRemoteAndLocalFile(
        ocFileEntity: OCFileEntity
    ): Long {
        val localFile: OCFileEntity? = getFileByOwnerAndRemotePath(
            owner = ocFileEntity.owner,
            remotePath = ocFileEntity.remotePath
        )
        if (localFile == null) {
            return insert(ocFileEntity)
        } else {
            // TODO: Handle conflicts, we will replace for the moment
            return insert(ocFileEntity.apply {
                id = localFile.id
                parentId = localFile.parentId
            })
        }
    }

    @Query(DELETE_FILE_WITH_ID)
    abstract fun deleteFileWithId(id: Long)

    companion object {
        private const val SELECT_FILE_WITH_ID =
            "SELECT * " +
                    "FROM ${ProviderMeta.ProviderTableMeta.FILES_TABLE_NAME} " +
                    "WHERE id = :id"

        private const val SELECT_FILE_FROM_OWNER_WITH_REMOTE_PATH =
            "SELECT * " +
                    "FROM ${ProviderMeta.ProviderTableMeta.FILES_TABLE_NAME} " +
                    "WHERE owner = :owner " +
                    "AND remotePath = :remotePath"

        private const val DELETE_FILE_WITH_ID =
            "DELETE FROM ${ProviderMeta.ProviderTableMeta.FILES_TABLE_NAME} " +
                    "WHERE id = :id"

        private const val SELECT_FOLDER_CONTENT =
            "SELECT * " +
                    "FROM ${ProviderMeta.ProviderTableMeta.FILES_TABLE_NAME} " +
                    "WHERE parentId = :folderId"

        private const val SELECT_FOLDER_BY_MIMETYPE =
            "SELECT * " +
                    "FROM ${ProviderMeta.ProviderTableMeta.FILES_TABLE_NAME} " +
                    "WHERE parentId = :folderId " +
                    "AND mimeType LIKE :mimeType || '%' "
    }
}