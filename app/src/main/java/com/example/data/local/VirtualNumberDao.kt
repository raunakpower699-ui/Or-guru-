package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VirtualNumberDao {
    @Query("SELECT * FROM virtual_numbers WHERE status = 'ACTIVE' ORDER BY assignedTimestamp DESC")
    fun getActiveNumbersFlow(): Flow<List<VirtualNumberEntity>>

    @Query("SELECT * FROM virtual_numbers ORDER BY status ASC, assignedTimestamp DESC")
    fun getAllNumbersFlow(): Flow<List<VirtualNumberEntity>>

    @Query("SELECT * FROM virtual_numbers WHERE id = :id LIMIT 1")
    fun getNumberByIdFlow(id: Long): Flow<VirtualNumberEntity?>

    @Query("SELECT * FROM virtual_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    fun getNumberByPhoneFlow(phoneNumber: String): Flow<VirtualNumberEntity?>

    @Query("SELECT * FROM virtual_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getNumberByPhone(phoneNumber: String): VirtualNumberEntity?

    @Query("SELECT COUNT(*) FROM virtual_numbers WHERE status = 'ACTIVE'")
    suspend fun getActiveNumbersCount(): Int

    @Query("SELECT * FROM virtual_numbers WHERE status = 'ACTIVE'")
    suspend fun getActiveNumbersList(): List<VirtualNumberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNumbers(numbers: List<VirtualNumberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNumber(number: VirtualNumberEntity): Long

    @Update
    suspend fun updateNumber(number: VirtualNumberEntity)

    @Query("UPDATE virtual_numbers SET status = 'EXPIRED' WHERE expiresAt <= :currentTime AND status = 'ACTIVE'")
    suspend fun markExpiredNumbers(currentTime: Long): Int

    @Query("SELECT * FROM virtual_numbers WHERE activationId = :activationId LIMIT 1")
    suspend fun getNumberByActivationId(activationId: String): VirtualNumberEntity?

    @Query("UPDATE virtual_numbers SET rentalStatus = :status WHERE activationId = :activationId")
    suspend fun updateRentalStatus(activationId: String, status: String)

    @Query("DELETE FROM virtual_numbers WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredNumbers(currentTime: Long): Int

    @Query("DELETE FROM virtual_numbers")
    suspend fun deleteAllNumbers()

    @Query("UPDATE virtual_numbers SET smsCount = (SELECT COUNT(*) FROM sms_otp_messages WHERE phoneNumber = virtual_numbers.phoneNumber)")
    suspend fun updateAllSmsCounts()
}
