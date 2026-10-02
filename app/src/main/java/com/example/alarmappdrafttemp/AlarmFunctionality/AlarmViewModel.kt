package com.example.alarmappdrafttemp.AlarmFunctionality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmappdrafttemp.DB.AlarmDao
import com.example.alarmappdrafttemp.DB.AlarmItem
import com.example.alarmappdrafttemp.DB.Alarm_Items_db
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val scheduler: ScheduleAlarm,
    private val alarmDao: AlarmDao
): ViewModel(){

    private val _alarms = MutableStateFlow(emptyList<Alarm_Items_db>())
    private val _loadingbar = MutableStateFlow(false)

    val alarms= _alarms.asStateFlow()
    val loadingbar = _loadingbar.asStateFlow()

    init {
        viewModelScope.launch {
            _alarms.value = alarmDao.getAlarmItems()
        }
    }
    fun GetAlarms(){
        viewModelScope.launch {
            _alarms.value = alarmDao.getAlarmItems()
        }
    }

    fun scheduleAlarm(item: AlarmItem, id:Int){
        if(item.turnedOn) {
            scheduler.schedule(item)
            viewModelScope.launch {
                _loadingbar.value = true
                alarmDao.insertAlarmItem(Alarm_Items_db(alarmitem = item, id))
                _alarms.value = alarmDao.getAlarmItems()
                delay(1000L)
                _loadingbar.value = false
            }
        }
        else if (!item.turnedOn && _alarms.value.any{it.id==id}){
            cancelAlarm(item)
            viewModelScope.launch {
                _loadingbar.value = true
                alarmDao.insertAlarmItem(Alarm_Items_db(alarmitem = item, id = id))
                _alarms.value = alarmDao.getAlarmItems()
                delay(1000L)
                _loadingbar.value = false
            }
        }
        else{
            return
        }
    }

    fun cancelAlarm(item: AlarmItem){
        scheduler.cancel(item)
    }

    fun deleteAlaram(item: AlarmItem){
        viewModelScope.launch {
            alarmDao.deleteAlarmItem(Alarm_Items_db(alarmitem = item))
        }

    }






}