package com.dermatoai.model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dermatoai.helper.Resource
import com.dermatoai.oauth.OauthPreferences
import com.dermatoai.repository.AnalyzeRepository
import com.dermatoai.room.DiagnoseRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    private val analyzeRepository: AnalyzeRepository,
    oauthPreferences: OauthPreferences
) : ViewModel() {

    private val userId = oauthPreferences.getUserId()

    @OptIn(ExperimentalCoroutinesApi::class)
    val history: LiveData<List<AnalyzeHistoryData>> = userId.flatMapLatest { userId ->
        if (userId != null) {
            analyzeRepository.getAllAnalyzeRecord(userId).map { records ->
                records.map {
                    AnalyzeHistoryData(it.id, it.time, it.issue, it.confidenceScore)
                }
            }
        } else {
            flowOf(emptyList())
        }
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAnalyzeResult(id: Int): LiveData<AnalyzeImageInfo?> = userId.flatMapLatest { userId ->
        if (userId != null) {
            analyzeRepository.getById(id, userId).map { record ->
                AnalyzeImageInfo(
                    record.confidenceScore,
                    record.issue,
                    record.time,
                    Uri.parse(record.image),
                    record.additionalInfo
                )
            }
        } else {
            flowOf(null)
        }
    }.asLiveData()


    @OptIn(ExperimentalCoroutinesApi::class)
    fun currentAnalyzeImage(image: Uri): LiveData<Resource<DiagnoseRecord>> =
        userId.flatMapLatest { userId ->
            if (userId != null) {
                analyzeRepository.analyzeImage(image, userId)
                    .catch {
                        emit(Resource.Error(it))
                    }
            } else {
                flowOf(Resource.Error(RuntimeException("User Id not exits")))
            }
        }.distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .asLiveData()

}